package Serveur;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;


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
    public void AcheterArticle(String refArticle, int qte) throws RemoteException, SQLException {
        String requeteRecupArticle = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID ='" + refArticle + "';";
        PreparedStatement requeteStatement = con.prepareStatement(requeteRecupArticle);

        String[] resultatRenvoye = new String[64];
        ResultSet resultats = requeteStatement.executeQuery(requeteRecupArticle);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nbCols = rsmd.getColumnCount();
        boolean encore = resultats.next();

        while(encore) {
            for(int i = 1; i<= nbCols; i++)
                resultatRenvoye[i] = resultats.getString(i); // récupère le contenu du ResultSetMetaData et le met dans un tableau de String

            encore = resultats.next();
        }

        int qteRenvoye = Integer.parseInt(resultatRenvoye[4]) - qte; // calcul de la nouvelle quantité
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()); //date du jour
        
        try{
            FileWriter writerFacture = new FileWriter("facture_" + timeStamp); //Mettre un chemin relatif
            writerFacture.write("Facture du "
                    + timeStamp + "\n" + resultatRenvoye[1] + " " + resultatRenvoye[2] + " " + resultatRenvoye[3]); // a tester
            writerFacture.close();
        } catch (IOException e){
            e.printStackTrace();
        }



        String requeteMajArticle = "UPDATE Article SET nb_total = " + qteRenvoye + " where reference_ID ='" + refArticle + "';";
        PreparedStatement requeteStatementMaj = con.prepareStatement(requeteMajArticle);

        int resultatUpdate = requeteStatementMaj.executeUpdate(requeteMajArticle);
        requeteStatement.close(); // fin de requete
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
    public String[] PayerFacture(String idFacture) throws IOException {
        String[] paye = new String[10];
        File file = new File(idFacture);
        Scanner reader = new Scanner(file);
        int i = 0;

        while(reader.hasNextLine()) {
            paye[i] = reader.nextLine();
            i++;
        }
        reader.close();// a réfléchir encore pour la suite de la fonction

        return paye;
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
    public void CalculerCA(String date) throws RemoteException {
        File file = new File("Fichier/")
        File[] listOfFiles = file.listFiles();

        for (File file : listOfFiles) {
        if (file.isFile() && file.getName.contains(date)) {
            
        }
        }
    }
}
