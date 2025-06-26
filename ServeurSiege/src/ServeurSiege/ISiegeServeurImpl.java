package ServeurSiege;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;

/**
 * Implémentation des méthodes de l'interface ISiegeServeur.
 * Ces méthodes font référence aux fonctions du serveur Siege.
 */
public class  ISiegeServeurImpl implements ISiegeServeur{

    private static String url = "jdbc:mysql://localhost:3306/articlessiege";
    private static Connection con = null;
    public ISiegeServeurImpl() throws RemoteException {
        super();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, "root", "root");
        } catch (SQLException s) {
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Met a jour les prix dans la base de données du serveur Siege.
     * @return HashMap<String, Float>
     * @throws RemoteException
     * @throws SQLException
     */
    @Override
    public HashMap<String, Float> miseAJourPrix() throws RemoteException, SQLException {
        HashMap<String, Float> map = new HashMap<>();

        String requeteMajPrix = "SELECT ref_article, prix FROM article";

        PreparedStatement requeteStatement = con.prepareStatement(requeteMajPrix);
        ResultSet resultats = requeteStatement.executeQuery();

        while (resultats.next()) {
            String reference = resultats.getString("ref_article");
            float prix = resultats.getFloat("prix");
            map.put(reference, prix);
            System.out.println(reference + " → " + prix);
        }

        return map;
    }

    /**
     * Fonction permettant de récupérer les fichiers envoyés par le serveur.
     * @param filename nom du fichier (facture)
     * @param data data passé en paramètre (in).
     * @throws RemoteException
     */
    @Override
    public void getFactures(String filename, byte[] data) throws RemoteException {
        try {
            FileOutputStream out = new FileOutputStream("ServeurSiege/Factures/" + filename);
                out.write(data);
                System.out.println("Fichier recu : " + filename);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
