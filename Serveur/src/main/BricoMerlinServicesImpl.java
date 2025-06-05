package Serveur;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


public class BricoMerlinServicesImpl implements IBricoMerlinServices{

    private static String url = "jdbc:mysql://localhost:3306/GestionArticles";
    private static Connection con = null;
    public BricoMerlinServicesImpl() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, "root","");
        } catch (SQLException s){
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
       /* finally { //A voir si ca doit rester ca

            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }

    @Override
    public String[] ConsulterArticle(String refArticle) throws RemoteException, SQLException {
        String requete = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID ='" + refArticle + "';";
        PreparedStatement requeteStatement = con.prepareStatement(requete);
        String[] resultatRenvoye = new String[5];
        ResultSet resultats = requeteStatement.executeQuery(requete);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nbCols = rsmd.getColumnCount();
        boolean encore = resultats.next();

        while (encore) {
            for (int i = 1; i <= nbCols; i++)
                resultatRenvoye[i] = resultats.getString(i);

            encore = resultats.next();
        }
        return resultatRenvoye;
    }

    @Override
    public String[] ConsulterFamille(String familleArticle) throws RemoteException, SQLException {
        String requete = "SELECT reference_ID, famille_article, prix_unitaire, nb_total " +
                "FROM Article, Famille " +
                "WHERE Famille.id_famille = Article.famille_article AND nom_famille = '" + familleArticle + "';";
        PreparedStatement requeteStatement = con.prepareStatement(requete);

        String[] resultatRenvoye = new String[64];
        ResultSet resultats = requeteStatement.executeQuery(requete);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nbCols = rsmd.getColumnCount();

        int index = 1;  // pour remplir le tableau à plat

        while (resultats.next()) {
            for (int col = 1; col <= nbCols; col++) {
                resultatRenvoye[index] = resultats.getString(col);
                index++;
            }
        }

        requeteStatement.close();
        return resultatRenvoye;
    }


    @Override
    public void AcheterArticle(String refArticle, int[] qte, boolean paye) throws RemoteException, SQLException {
        String requeteRecupArticle = "SELECT reference_ID, prix_unitaire, nb_total FROM Article WHERE reference_ID IN (" + refArticle + ")";

        try (
                PreparedStatement requeteStatement = con.prepareStatement(requeteRecupArticle);
                ResultSet resultats = requeteStatement.executeQuery()
        ) {
            ResultSetMetaData rsmd = resultats.getMetaData();
            int nbCols = rsmd.getColumnCount();

            List<String[]> lignesArticles = new ArrayList<>();
            List<String> refs = new ArrayList<>();
            List<Integer> stocks = new ArrayList<>();
            List<Float> prixUnitaires = new ArrayList<>();

            while (resultats.next()) {
                String[] ligne = new String[nbCols];
                for (int i = 0; i < nbCols; i++) {
                    ligne[i] = resultats.getString(i + 1);
                }
                lignesArticles.add(ligne);
                refs.add(resultats.getString("reference_ID"));
                stocks.add(resultats.getInt("nb_total"));
                prixUnitaires.add(resultats.getFloat("prix_unitaire"));
            }

            // Génération de la facture
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String nomFacture = "facture_" + timeStamp;
            try (FileWriter writerFacture = new FileWriter(nomFacture)) {
                writerFacture.write("Facture du " + timeStamp + "\n");

                float montantTotal = 0;
                for (int i = 0; i < lignesArticles.size(); i++) {
                    String[] ligne = lignesArticles.get(i);
                    for (int j = 0; j < nbCols; j++) {
                      /* if(Objects.equals(rsmd.getColumnLabel(j), "nb_total")){
                            continue;
                       }*/
                        writerFacture.write(rsmd.getColumnLabel(j + 1) + " : " + ligne[j] + "\n");
                    }
                    writerFacture.write("Quantité : " + qte[i] + "\n");
                    float prixTotal = qte[i] * prixUnitaires.get(i);
                    writerFacture.write("Prix total : " + prixTotal + "\n---\n");

                    montantTotal = montantTotal + prixTotal;
                }
                writerFacture.write("---\n Total : " + montantTotal);

                String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                System.out.println(date);
                String requeteCreationFacture = "INSERT INTO Facture (facture_ID, paye, montant, date_facture) VALUES ('"+ nomFacture +"', "+ paye +", " + montantTotal + ", '" + date + "')";
                PreparedStatement requeteStatementMaj = con.prepareStatement(requeteCreationFacture);
                int resultatInsert = requeteStatementMaj.executeUpdate(requeteCreationFacture);
                requeteStatement.close(); // fin de requete

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Mise à jour des quantités en stock
            for (int i = 0; i < refs.size(); i++) {
                String ref = refs.get(i);
                int stockDisponible = stocks.get(i);
                int qteDemandee = qte[i];

                if (qteDemandee > stockDisponible) {
                    throw new RemoteException("Stock insuffisant pour l'article : " + ref);
                }

                String requeteMajArticle = "UPDATE Article SET nb_total = nb_total - ? WHERE reference_ID = ?";
                try (PreparedStatement requeteStatementMaj = con.prepareStatement(requeteMajArticle)) {
                    requeteStatementMaj.setInt(1, qteDemandee);
                    requeteStatementMaj.setString(2, ref);
                    requeteStatementMaj.executeUpdate();
                }
            }
        }
    }


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
    public String[] PayerFacture(String idFacture) throws IOException, SQLException {
        String[] facture = new String[256];
        File file = new File(idFacture);
        Scanner reader = new Scanner(file);
        int i = 0;

        while(reader.hasNextLine()) {
            facture[i] = reader.nextLine();
            i++;
        }
        reader.close();// a réfléchir encore pour la suite de la fonction

        String requeteMajFacture = "UPDATE Facture SET paye = 1 WHERE facture_ID = '" + idFacture + "';";
        PreparedStatement requeteStatementMaj = con.prepareStatement(requeteMajFacture);
        int resultatUpdate = requeteStatementMaj.executeUpdate(requeteMajFacture);
        requeteStatementMaj.close();

        return facture;
    }

    @Override
    public String[] ConsulterFacture(String idFacture) throws RemoteException {
        String[] dataFacture = new String[64];
        int i =0;
        try {
            Scanner readerFacture = new Scanner(new File(idFacture));
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

    @Override
    public float CalculerCA(String date) throws RemoteException, SQLException {
        String requeteRecupeFactureJournee = "SELECT montant FROM Facture WHERE date_facture = '" + date + "' AND paye = 1;";
        PreparedStatement requeteStatement = con.prepareStatement(requeteRecupeFactureJournee);
        String[] resultatRenvoye = new String[64];
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
}
