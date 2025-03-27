package Serveur;
import java.rmi.RemoteException;
import java.sql.*;


public class BricoMerlinServicesImpl implements IBricoMerlinServices{

    private static String url = "jdbc:mysql://localhost:3306/GestionArticles";
    private static Connection con = null;
    public BricoMerlinServicesImpl() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, "root","");
        } catch (SQLException s){
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String[] ConsulterArticle(String refArticle) throws RemoteException, SQLException {
        String requete = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID =" + refArticle + ";";
        String[] resultatRenvoye = new String[4];
        Statement requeteStatement = con.createStatement();
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
    public void ConsulterFamille(String familleArticle) throws RemoteException {

    }

    @Override
    public void AcheterArticle(String refArticle, int qte) throws RemoteException {

    }

    @Override
    public void AjouterStockArticle(String refArticle, int qte) throws RemoteException {

    }

    @Override
    public void PayerFacture(String idFacture) throws RemoteException {

    }

    @Override
    public void ConsulterFacture(String idFacture) throws RemoteException {

    }

    @Override
    public void CalculerCA(String date) throws RemoteException {

    }
}
