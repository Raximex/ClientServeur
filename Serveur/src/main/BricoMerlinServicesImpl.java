package Serveur;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Classe implémentant les méthodes de l'interface IBricoMerlinServices.
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


    @Override
    public List<String> consulterArticle(String refArticle) throws RemoteException, SQLException {
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

    @Override
    public List<String> consulterFamille(String familleArticle) throws RemoteException, SQLException {
        // Récupère la liste des articles d’une famille donnée
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
     * Achète une liste d'articles avec leurs quantités, vérifie la disponibilité du stock,
     * génère une facture (fichier + base de données), met à jour le stock,
     * et enregistre le paiement si demandé.
     */
    @Override
    public void acheterArticle(List<String> refArticle, List<Integer> qte, boolean paye) throws RemoteException, SQLException {
        // Vérifie que la liste des références d'articles n'est pas vide
        if (refArticle == null || refArticle.isEmpty()) {
            throw new RemoteException("Aucune référence fournie.");
        }

        // Crée une chaîne de "?" pour préparer la requête SQL IN (?,?,...),
        // selon le nombre d'articles demandés
        String placeholders = String.join(",", Collections.nCopies(refArticle.size(), "?"));
        String requeteRecupArticle = "SELECT reference_ID, prix_unitaire, nb_total FROM Article WHERE reference_ID IN (" + placeholders + ")";

        try (
                // Prépare la requête SQL
                PreparedStatement stmt = con.prepareStatement(requeteRecupArticle)
        ) {
            // Remplit les paramètres de la requête avec les références fournies
            for (int i = 0; i < refArticle.size(); i++) {
                stmt.setString(i + 1, refArticle.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                // Listes temporaires pour stocker les données récupérées
                List<String> refs = new ArrayList<>();
                List<Integer> stocks = new ArrayList<>();
                List<Float> prixUnitaires = new ArrayList<>();

                // Parcourt le résultat de la requête pour récupérer les infos des articles
                while (rs.next()) {
                    refs.add(rs.getString("reference_ID"));
                    stocks.add(rs.getInt("nb_total"));
                    prixUnitaires.add(rs.getFloat("prix_unitaire"));
                }

                // Vérifie que toutes les références demandées ont été trouvées
                if (refs.size() != refArticle.size()) {
                    throw new RemoteException("Une ou plusieurs références sont introuvables.");
                }

                // Vérifie que le stock est suffisant pour chaque article demandé
                for (int i = 0; i < refs.size(); i++) {
                    if (qte.get(i) > stocks.get(i)) {
                        throw new RemoteException("Stock insuffisant pour l'article : " + refs.get(i));
                    }
                }

                // Génère un nom de facture unique basé sur la date et l'heure
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                String nomFacture = "facture_" + timeStamp;
                String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

                float montantTotal = 0;

                // DÉBUT DE LA TRANSACTION SQL pour garantir la cohérence des opérations
                con.setAutoCommit(false);
                try {
                    // Écriture de la facture dans un fichier texte
                    try (FileWriter writer = new FileWriter("Serveur/Factures/" + nomFacture)) {
                        writer.write("Facture du " + date + "\n---\n");

                        // Détaille chaque article avec sa quantité et son prix total
                        for (int i = 0; i < refs.size(); i++) {
                            writer.write("Référence : " + refs.get(i) + "\n");
                            writer.write("Prix unitaire : " + prixUnitaires.get(i) + "\n");
                            writer.write("Quantité : " + qte.get(i) + "\n");
                            float total = prixUnitaires.get(i) * qte.get(i);
                            writer.write("Prix total : " + total + "\n---\n");

                            montantTotal += total; // Calcule le montant total de la facture
                        }

                        writer.write("TOTAL : " + montantTotal);
                    } catch (IOException e) {
                        // En cas d’erreur d’écriture, on annule la transaction
                        con.rollback();
                        throw new RemoteException("Erreur lors de l'écriture de la facture.");
                    }

                    // Insertion de la facture dans la base de données
                    String insertFactureSQL = "INSERT INTO Facture (facture_ID, paye, montant, date_facture) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertStmt = con.prepareStatement(insertFactureSQL)) {
                        insertStmt.setString(1, nomFacture);
                        insertStmt.setBoolean(2, paye);
                        insertStmt.setFloat(3, montantTotal);
                        insertStmt.setString(4, date);
                        insertStmt.executeUpdate();
                    }

                    // Mise à jour des stocks en base pour chaque article acheté
                    String updateStockSQL = "UPDATE Article SET nb_total = nb_total - ? WHERE reference_ID = ?";
                    try (PreparedStatement updateStmt = con.prepareStatement(updateStockSQL)) {
                        for (int i = 0; i < refs.size(); i++) {
                            updateStmt.setInt(1, qte.get(i));
                            updateStmt.setString(2, refs.get(i));
                            updateStmt.executeUpdate();
                        }
                    }

                    // Validation de la transaction SQL (commit)
                    con.commit();
                } catch (SQLException | RemoteException e) {
                    // En cas d'erreur pendant la transaction, rollback pour annuler toutes les opérations
                    con.rollback();
                    throw e;
                } finally {
                    // Rétablissement du mode autocommit
                    con.setAutoCommit(true);
                }
            }
        }
    }

    @Override
    public void ajouterStockArticle(String refArticle, int qte) throws RemoteException, SQLException {
        // Récupération des informations correspondant à l'article demandé
        String requeteRecupArticle = "SELECT nb_total FROM Article WHERE reference_ID = ?";

        try {
            con.setAutoCommit(false); // début transaction SQL

            try (PreparedStatement stmt = con.prepareStatement(requeteRecupArticle)) {
                stmt.setString(1, refArticle);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new RemoteException("Article introuvable : " + refArticle);
                    }

                    int stockActuel = rs.getInt("nb_total");
                    int nouveauStock = stockActuel + qte;

                    String requeteMajArticle = "UPDATE Article SET nb_total = ? WHERE reference_ID = ?";

                    try (PreparedStatement updateStmt = con.prepareStatement(requeteMajArticle)) {
                        updateStmt.setInt(1, nouveauStock);
                        updateStmt.setString(2, refArticle);
                        updateStmt.executeUpdate();
                    }
                }
            }

            con.commit(); // validation transaction SQL
        } catch (SQLException | RemoteException e) {
            con.rollback(); // annulation en cas d'erreur
            throw e;
        } finally {
            con.setAutoCommit(true); // remise à l'état par défaut
        }
    }

    @Override
    public Map<String, Integer> getArticlesAvecStock() throws RemoteException, SQLException {
        Map<String, Integer> stockMap = new HashMap<>();

        String sql = "SELECT reference_ID, nb_total FROM Article";

        // Exécution de la requête pour récupérer la référence et le stock de chaque article
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

        // Récupération de toutes les références d'articles dans la base
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

        // Exécute la requête pour récupérer tous les noms de famille d'articles
        String sql = "SELECT nom_famille FROM Famille";
        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                refs.add(resultats.getString("nom_famille"));
            }
        }
        return refs;
    }

    @Override
    public void payerFacture(String idFacture) throws IOException, SQLException {
        // Exécute la requête pour mettre à jour l'état du paiement dans la base de donnée
        String requeteMajFacture = "UPDATE Facture SET paye = 1 WHERE facture_ID = ?";
        try (PreparedStatement stmt = con.prepareStatement(requeteMajFacture)) {
            stmt.setString(1, idFacture);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> consulterFacture(String idFacture) throws RemoteException {
        List<String> dataFacture = new ArrayList<>();
        // Lecture de la facture
        try (Scanner readerFacture = new Scanner(new File("Serveur/Factures/" + idFacture))) {
            while (readerFacture.hasNextLine()) {
                dataFacture.add(readerFacture.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFacture;
    }

    @Override
    public float calculerCA(String date) throws RemoteException, SQLException {
        // Récupération des factures à la date demandée
        String sql = "SELECT montant FROM Facture WHERE date_facture = ? AND paye = 1";
        float ca = 0;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, date);
            try (ResultSet resultats = stmt.executeQuery()) {
                while (resultats.next()) {
                    // Calcul du CA
                    ca += resultats.getFloat("montant");
                }
            }
        }
        return ca;
    }

    @Override
    public List<String> getFacturesNonPayees() throws RemoteException, SQLException {
        List<String> factures = new ArrayList<>();
        // Récupération des factures non payées
        String sql = "SELECT facture_ID FROM Facture WHERE paye = 0";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                factures.add(resultats.getString("facture_ID"));
            }
        }
        return factures;
    }

    @Override
    public List<String> getFactures() throws RemoteException, SQLException {
        List<String> factures = new ArrayList<>();
        // Récupération des factures
        String sql = "SELECT facture_ID FROM Facture";

        try (PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet resultats = stmt.executeQuery()) {
            while (resultats.next()) {
                factures.add(resultats.getString("facture_ID"));
            }
        }
        return factures;
    }

    public static void miseAJourServeur(HashMap<String, Float> map) throws RemoteException, SQLException {
        // Mise à jour des prix du serveur
        String sql = "UPDATE Article SET prix_unitaire = ? WHERE reference_ID = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                String ref = entry.getKey();
                Float nouveauPrix = entry.getValue();
                stmt.setFloat(1, nouveauPrix);
                stmt.setString(2, ref);
                stmt.executeUpdate();
            }
        }
    }
}
