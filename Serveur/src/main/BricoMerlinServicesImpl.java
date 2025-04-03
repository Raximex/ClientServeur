package Serveur;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Scanner;


public class BricoMerlinServicesImpl extends UnicastRemoteObject implements IBricoMerlinServices{

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
    public String[] ConsulterFamille(String familleArticle) throws RemoteException, SQLException {
        String requete = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article,Famille where famille_article=id_famille and nom_famille =" + familleArticle +";";
        String[] resultatRenvoye = new String[1024];
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
        requeteStatement.close(); // fin de requete

        return resultatRenvoye;
    }

    @Override
    public void AcheterArticle(String refArticle, int qte) throws RemoteException, SQLException {
        String requeteRecupArticle = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID =" + refArticle +";";

        String[] resultatRenvoye = new String[64];
        Statement requeteStatement = con.createStatement();
        ResultSet resultats = requeteStatement.executeQuery(requeteRecupArticle);
        ResultSetMetaData rsmd = resultats.getMetaData();
        int nbCols = rsmd.getColumnCount();
        boolean encore = resultats.next();

        while(encore) {
            for(int i = 1; i<= nbCols; i++)
                resultatRenvoye[i] = resultats.getString(i); // récupère le contenu du ResultSetMetaData et le met dans un tableau de String

            encore = resultats.next();
        }

        int qteRenvoye = Integer.parseInt(resultatRenvoye[3]) - qte; // calcul de la nouvelle quantité
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()); //date du jour
        
        try{
            FileWriter writerFacture = new FileWriter("facture_" + timeStamp); //Mettre un chemin relatif
            writerFacture.write("Facture du "
                    + timeStamp + "\n" + resultatRenvoye[0] + " " + resultatRenvoye[1] + " " + resultatRenvoye[2]); // a tester
            writerFacture.close();
        } catch (IOException e){
            e.printStackTrace();
        }



        String requeteMajArticle = "UPDATE Article SET nb_total = " + qteRenvoye + "where reference_ID = " + refArticle + ";";
        Statement requeteStatementUpdate = con.createStatement();
        ResultSet resultatUpdate = requeteStatementUpdate.executeQuery(requeteRecupArticle);

        requeteStatement.close(); // fin de requete

    }

    @Override
    public void AjouterStockArticle(String refArticle, int qte) throws RemoteException, SQLException {
    String requeteRecupArticle = "Select reference_ID, famille_article, prix_unitaire, nb_total from Article where reference_ID =" + refArticle +";";

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

        int qteRenvoye = Integer.parseInt(resultatRenvoye[3]) + qte; //Nouvel quantité du stock après ajout de stock

        String requeteMajArticle = "UPDATE Article SET nb_total = " + qteRenvoye + "where reference_ID = " + refArticle + ";";
        Statement requeteStatementUpdate = con.createStatement();
        ResultSet resultatUpdate = requeteStatementUpdate.executeQuery(requeteRecupArticle);
    
        requeteStatement.close(); // fin de requete
    }

    @Override
    public void PayerFacture(String idFacture) throws RemoteException {

    }

    @Override
    public String ConsulterFacture(String idFacture) throws RemoteException {
        String dataFacture = null;
        try {
            dataFacture = null;
            Scanner readerFacture = new Scanner(new File(idFacture));
            while (readerFacture.hasNextLine()) {
                dataFacture = readerFacture.nextLine();
            }
            readerFacture.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataFacture;
    }

    @Override
    public void CalculerCA(String date) throws RemoteException {

    }
}
