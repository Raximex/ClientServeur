package Serveur;
import java.rmi.RemoteException;
import java.sql.*;


public class BricoMerlinServicesImpl implements IBricoMerlinServices{

    private static String url = "jdbc:mysql://localhost:3306/GestionArticles";

    public static void main(String[] args) {

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, "root","");
            Statement requete = con.createStatement();
            ResultSet resultats = requete.executeQuery("Select * from Article;");
            ResultSetMetaData rsmd = resultats.getMetaData();
            int nbCols = rsmd.getColumnCount();
            boolean encore = resultats.next();

            while (encore) {

                for (int i = 1; i <= nbCols; i++)
                    System.out.print(resultats.getString(i) + " ");
                System.out.println();
                encore = resultats.next();
            }
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
    public void ConsulterArticle(String refArticle) throws RemoteException {

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
