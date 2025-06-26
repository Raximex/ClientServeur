package Serveur;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

// Créer l'interface de l'objet distant
public interface IBricoMerlinServices extends Remote {  
   //#region Article
   //Récupère les informations de l'article en passant sa référence article.
   List<String> ConsulterArticle(String refArticle) throws RemoteException, SQLException;

   //Récupère la liste d'article en fonction de sa famille. Seul les articles avec un stock > 0 sont récupérés.
   List<String> ConsulterFamille(String familleArticle) throws RemoteException, SQLException;

   //Ajoute l'article demandé à la facture et retire du stock la quantité demandée.
   void AcheterArticle(List<String> refArticle, List<Integer> qte, boolean paye) throws RemoteException, SQLException;

   //Ajoute une quantité de stock à une référence article. La référence doit déjà exister.
   void AjouterStockArticle(String refArticle, int qte) throws RemoteException, SQLException;

   //Récupère la liste des articles et leur stock
   Map<String, Integer> getArticlesAvecStock() throws RemoteException, SQLException;

   //Récupère la liste des articles
   List<String> getArticles() throws RemoteException, SQLException;

   //Récupère la liste des familles
   List<String> getFamilles() throws RemoteException, SQLException;

   //#endregion

   //#region Facture
   //Permet à un client de payer sa facture.
   String[] PayerFacture(String idFacture) throws IOException, SQLException;

   //Récupère les informations de la facture (ticket de caisse) demandée.
   String[] ConsulterFacture(String idFacture) throws RemoteException;

   //Calcule le chiffre d’affaire à une date donnée en fonction des factures de cette date.
   float CalculerCA(String date) throws RemoteException, SQLException;

   //#endregion
}