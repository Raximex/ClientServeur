package Serveur;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe implémentant les méthode de l'interface IBricoMerlinServices.
 * Cette classe possède les méthodes du serveur.
 */
public class BricoMerlinServicesImpl implements IBricoMerlinServices{

    private static String url = "jdbc:mysql://localhost:3306/GestionArticles";
    private static Connection con = null;
    public BricoMerlinServicesImpl() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, "root","root");
        } catch (SQLException s){
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Permet de consulter un article grâce a sa référence.
     * @param refArticle référence de l'article
     * @return un tableau de String qui permet d'effectuer l'affichage des détails de l'article.
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public List<String> ConsulterArticle(String refArticle) throws RemoteException, SQLException {
        List<String> resultat = new ArrayList<>();

        String requete = "SELECT reference_ID, nom_famille, prix_unitaire, nb_total " +
                "FROM Article, Famille " +
                "WHERE Article.famille_article = Famille.id_famille AND reference_ID = ?";

        try (PreparedStatement stmt = con.prepareStatement(requete)) {
            stmt.setString(1, refArticle);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    resultat.add(rs.getString("reference_ID"));
                    resultat.add(rs.getString("nom_famille"));
                    resultat.add(rs.getString("prix_unitaire"));
                    resultat.add(rs.getString("nb_total"));
                }
            }
        }
        return resultat;
    }

    /**
     * Fonction permettant de consulter tous les articles en donnant une famille d'article.
     * @param familleArticle
     * @return
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public List<String> ConsulterFamille(String familleArticle) throws RemoteException, SQLException {
        List<String> resultat = new ArrayList<>();

        String requete = "SELECT reference_ID, famille_article, prix_unitaire, nb_total " +
                "FROM Article, Famille " +
                "WHERE Famille.id_famille = Article.famille_article AND nom_famille = ?";

        try (PreparedStatement stmt = con.prepareStatement(requete)) {
            stmt.setString(1, familleArticle);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultat.add(rs.getString("reference_ID"));
                    resultat.add(rs.getString("famille_article"));
                    resultat.add(rs.getString("prix_unitaire"));
                    resultat.add(rs.getString("nb_total"));
                }
            }
        }

        return resultat;
    }

    /**
     * Permet d'acheter un article ou plusieurs.
     * @param refArticle
     * @param qte
     * @param paye
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public void AcheterArticle(List<String> refArticle, List<Integer> qte, boolean paye) throws RemoteException, SQLException {
        if (refArticle == null || refArticle.isEmpty()) {
            throw new RemoteException("Aucune référence fournie.");
        }

        String placeholders = String.join(",", Collections.nCopies(refArticle.size(), "?"));
        String requeteRecupArticle = "SELECT reference_ID, prix_unitaire, nb_total FROM Article WHERE reference_ID IN (" + placeholders + ")";

        try (
                PreparedStatement stmt = con.prepareStatement(requeteRecupArticle)
        ) {
            for (int i = 0; i < refArticle.size(); i++) {
                stmt.setString(i + 1, refArticle.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> refs = new ArrayList<>();
                List<Integer> stocks = new ArrayList<>();
                List<Float> prixUnitaires = new ArrayList<>();

                while (rs.next()) {
                    refs.add(rs.getString("reference_ID"));
                    stocks.add(rs.getInt("nb_total"));
                    prixUnitaires.add(rs.getFloat("prix_unitaire"));
                }

                if (refs.size() != refArticle.size()) {
                    throw new RemoteException("Une ou plusieurs références sont introuvables.");
                }

                for (int i = 0; i < refs.size(); i++) {
                    if (qte.get(i) > stocks.get(i)) {
                        throw new RemoteException("Stock insuffisant pour l'article : " + refs.get(i));
                    }
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                String nomFacture = "facture_" + timeStamp;
                String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                float montantTotal = 0;

                try (FileWriter writer = new FileWriter("Serveur/Factures/" + nomFacture)) {
                    writer.write("Facture du " + date + "\n---\n");

                    for (int i = 0; i < refs.size(); i++) {
                        writer.write("Référence : " + refs.get(i) + "\n");
                        writer.write("Prix unitaire : " + prixUnitaires.get(i) + "\n");
                        writer.write("Quantité : " + qte.get(i) + "\n");
                        float total = prixUnitaires.get(i) * qte.get(i);
                        writer.write("Prix total : " + total + "\n---\n");

                        montantTotal += total;
                    }

                    writer.write("TOTAL : " + montantTotal);
                } catch (IOException e) {
                    throw new RemoteException("Erreur lors de l'écriture de la facture.");
                }

                String insertFactureSQL = "INSERT INTO Facture (facture_ID, paye, montant, date_facture) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = con.prepareStatement(insertFactureSQL)) {
                    insertStmt.setString(1, nomFacture);
                    insertStmt.setBoolean(2, paye);
                    insertStmt.setFloat(3, montantTotal);
                    insertStmt.setString(4, date);
                    insertStmt.executeUpdate();
                }

                String updateStockSQL = "UPDATE Article SET nb_total = nb_total - ? WHERE reference_ID = ?";
                try (PreparedStatement updateStmt = con.prepareStatement(updateStockSQL)) {
                    for (int i = 0; i < refs.size(); i++) {
                        updateStmt.setInt(1, qte.get(i));
                        updateStmt.setString(2, refs.get(i));
                        updateStmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Permet d'ajouter du stock a un article.
     * @param refArticle référence de l'article.
     * @param qte qte a mettre à jour dans la base
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public void AjouterStockArticle(String refArticle, int qte) throws RemoteException, SQLException {
    String requeteRecupArticle = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID ='" + refArticle +"';";

        String[] resultatRenvoye = new String[64];
        Statement requeteStatement = con.createStatement();
        ResultSet resultats = requeteStatement.executeQuery(requeteRecupArticle);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nbCols = rsmd.getColumnCount();
        boolean encore = resultats.next();

        while(encore) {
            for(int i = 1; i<= nbCols; i++)
                resultatRenvoye[i] = resultats.getString(i);

            encore = resultats.next();
        }

        int qteRenvoye = Integer.parseInt(resultatRenvoye[4]) + qte; //Nouvel quantité du stock après ajout de stock

        String requeteMajArticle = "UPDATE Article SET nb_total = " + qteRenvoye + " where reference_ID = '" + refArticle + "';";

        PreparedStatement requeteStatementMaj = con.prepareStatement(requeteMajArticle);

        int resultatUpdate = requeteStatementMaj.executeUpdate(requeteMajArticle);

        requeteStatement.close(); // fin de requete
    }

    @Override
    public Map<String, Integer> getArticlesAvecStock() throws RemoteException, SQLException {
        Map<String, Integer> stockMap = new LinkedHashMap<>();
        String sql = "SELECT reference_ID, nb_total FROM Article";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                stockMap.put(resultats.getString("reference_ID"), resultats.getInt("nb_total"));
            }
        }
        return stockMap;
    }

    @Override
    public List<String> getArticles() throws RemoteException, SQLException {
        List<String> refs = new ArrayList<>();
        String sql = "SELECT reference_ID FROM Article";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                refs.add(resultats.getString("reference_ID"));
            }
        }
        return refs;
    }

    @Override
    public List<String> getFamilles() throws RemoteException, SQLException {
        List<String> refs = new ArrayList<>();
        String sql = "SELECT nom_famille FROM Famille";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                refs.add(resultats.getString("nom_famille"));
            }
        }
        return refs;
    }

    /**
     * Permet de payer une facture.
     * @param idFacture
     * @return
     * @throws IOException
     * @throws SQLException
     */
    @Override
    public String[] PayerFacture(String idFacture) throws IOException, SQLException {
        String[] facture = new String[256];
        File file = new File("Serveur/Factures" + idFacture);
        Scanner reader = new Scanner(file);
        int i = 0;

        while(reader.hasNextLine()) {
            facture[i] = reader.nextLine();
            i++;
        }
        reader.close();// a réfléchir encore pour la suite de la fonction

        String requeteMajFacture = "UPDATE Facture SET paye = 1 WHERE facture_ID = '" + idFacture + "';";
        PreparedStatement requeteStatementMaj = con.prepareStatement(requeteMajFacture);
        requeteStatementMaj.executeUpdate(requeteMajFacture);
        requeteStatementMaj.close();

        return facture;
    }

    /**
     * Permet de consulter une facture, il faut passer en paramètres un id de facture pour que celui soit lu.
     * @param idFacture
     * @return
     * @throws RemoteException
     */
    @Override
    public String[] ConsulterFacture(String idFacture) throws RemoteException {
        String[] dataFacture = new String[64];
        int i =0;
        try {
            Scanner readerFacture = new Scanner(new File("Serveur/Factures" + idFacture));
            while (readerFacture.hasNextLine()) {
                dataFacture[i] = readerFacture.nextLine();
                i++;
            }
            readerFacture.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFacture;
    }

    /**
     * Calcule le chiffre d'affaire de la journée choisi.
     * @param date
     * @return
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public float CalculerCA(String date) throws RemoteException, SQLException {
        String requeteRecupeFactureJournee = "SELECT montant FROM Facture WHERE date_facture = '" + date + "' AND paye = 1;";
        PreparedStatement requeteStatement = con.prepareStatement(requeteRecupeFactureJournee);
        ResultSet resultats = requeteStatement.executeQuery(requeteRecupeFactureJournee);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nCols = rsmd.getColumnCount();

        float ca = 0;
        while(resultats.next()) {
            for(int i = 1; i<= nCols; i++){
                ca = ca + resultats.getFloat("montant");
            }
        }
        return ca;
    }

    /**
     * Permet de mettre à jour les prix du serveur.
     * @param map Hashmap clé valeur avec une référence d'article et un prix.
     * @throws RemoteException
     * @throws SQLException
     */
    public static void MiseAJourServeur(HashMap<String,Float> map) throws RemoteException, SQLException {
        for(Object key : map.keySet()) {
            System.out.println(key + " : " + map.get(key));
            String requete = "UPDATE article SET prix_unitaire = '" + map.get(key) +"' WHERE reference_ID = '" + key + "';";
            PreparedStatement requeteStatementMaj = con.prepareStatement(requete);
            requeteStatementMaj.executeUpdate(requete);
            requeteStatementMaj.close();
        }
    }
}
