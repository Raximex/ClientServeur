import java.rmi.Remote; 
import java.rmi.RemoteException;  

// Créer l'interface de l'objet distant
public interface IBricoMerlinServices extends Remote {  
   //#region Article
   //Récupère les informations de l'article en passant sa référence article.
   void ConsulterArticle(string refArticle) throws RemoteException;  

   //Récupère la liste d'article en fonction de sa famille. Seul les articles avec un stock > 0 sont récupérés.
   void ConsulterFamille(string familleArticle) throws RemoteException;

   //Ajoute l'article demandé à la facture et retire du stock la quantité demandée.
   void AcheterArticle(string refArticle, int qte) throws RemoteException;

   //Ajoute une quantité de stock à une référence article. La référence doit déjà exister.
   void AjouterStockArticle(string refArticle, int qte) throws RemoteException;

   //#endregion

   //#region Facture
   //Permet à un client de payer sa facture.
   void PayerFacture(string idFacture) throws RemoteException;

   //Récupère les informations de la facture (ticket de caisse) demandée.
   void ConsulterFacture(string idFacture) throws RemoteException;

   //Calcule le chiffre d’affaire à une date donnée en fonction des factures de cette date.
   void CalculerCA(string date) throws RemoteException;

   //#endregion
}