package Serveur;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;

// Créer l'interface de l'objet distant
public interface IBricoMerlinServices extends Remote {  
   //#region Article
   //Récupère les informations de l'article en passant sa référence article.
   String[] ConsulterArticle(String refArticle) throws RemoteException, SQLException;

   //Récupère la liste d'article en fonction de sa famille. Seul les articles avec un stock > 0 sont récupérés.
   String[] ConsulterFamille(String familleArticle) throws RemoteException, SQLException;

   //Ajoute l'article demandé à la facture et retire du stock la quantité demandée.
   void AcheterArticle(String refArticle, int qte) throws RemoteException, SQLException;

   //Ajoute une quantité de stock à une référence article. La référence doit déjà exister.
   void AjouterStockArticle(String refArticle, int qte) throws RemoteException;

   //#endregion

   //#region Facture
   //Permet à un client de payer sa facture.
   void PayerFacture(String idFacture) throws RemoteException;

   //Récupère les informations de la facture (ticket de caisse) demandée.
   String ConsulterFacture(String idFacture) throws RemoteException;

   //Calcule le chiffre d’affaire à une date donnée en fonction des factures de cette date.
   void CalculerCA(String date) throws RemoteException;

   //#endregion
}