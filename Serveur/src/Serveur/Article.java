package Serveur;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Article {

    private static String url = "jdbc:mysql://localhost:3306/GestionArticles";

    public static void main() {

        Connection con = null;
        try {
            con = DriverManager.getConnection(url, "root", "");


        } catch (SQLException s){
            s.printStackTrace();
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
