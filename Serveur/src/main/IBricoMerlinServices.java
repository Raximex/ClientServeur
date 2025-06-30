package Serveur;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Interface RMI pour le service BricoMerlin côté serveur.
 * Définit les opérations distantes liées aux articles et aux factures.
 */
public interface IBricoMerlinServices extends Remote {

   // Article-related methods

   /**
    * Récupère les informations d'un article par sa référence.
    * @param refArticle référence de l'article
    * @return liste des détails de l'article
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> consulterArticle(String refArticle) throws RemoteException, SQLException;

   /**
    * Récupère la liste des articles d'une famille avec un stock > 0.
    * @param familleArticle nom de la famille
    * @return liste des articles
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> consulterFamille(String familleArticle) throws RemoteException, SQLException;

   /**
    * Achète une liste d'articles avec quantités, et retire du stock.
    * @param refArticle liste des références articles
    * @param qte quantités correspondantes
    * @param paye true si paiement immédiat
    * @return l'identifiant de la facture créée
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   String acheterArticle(List<String> refArticle, List<Integer> qte, boolean paye) throws RemoteException, SQLException;

   /**
    * Ajoute une quantité de stock à un article existant.
    * @param refArticle référence de l'article
    * @param qte quantité à ajouter
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   void ajouterStockArticle(String refArticle, int qte) throws RemoteException, SQLException;

   /**
    * Récupère la liste des articles avec leur stock.
    * @return map référence -> stock
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   Map<String, Integer> getArticlesAvecStock() throws RemoteException, SQLException;

   /**
    * Récupère la liste des références d'articles.
    * @return liste des références
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> getArticles() throws RemoteException, SQLException;

   /**
    * Récupère la liste des familles d'articles.
    * @return liste des familles
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> getFamilles() throws RemoteException, SQLException;

   // Facture-related methods

   /**
    * Permet à un client de payer une facture.
    * @param idFacture identifiant de la facture
    * @param modePaiement mode de paiement de la facture
    * @throws IOException en cas d'erreur d'entrée/sortie
    * @throws SQLException en cas d'erreur SQL
    */
   void payerFacture(String idFacture, String modePaiement) throws IOException, SQLException;

   /**
    * Affiche le contenu d'une facture sélectionnée.
    * @param idFacture identifiant de la facture
    * @return liste des lignes de la facture
    * @throws RemoteException en cas d'erreur RMI
    */
   List<String> consulterFacture(String idFacture) throws RemoteException;

   /**
    * Calcule le chiffre d'affaires pour une date donnée.
    * @param date date au format yyyy-MM-dd
    * @return chiffre d'affaires
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   float calculerCA(String date) throws RemoteException, SQLException;

   /**
    * Récupère la liste des factures non payées.
    * @return liste des identifiants de factures non payées
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> getFacturesNonPayees() throws RemoteException, SQLException;

   /**
    * Récupère la liste de toutes les factures.
    * @return liste des identifiants de factures
    * @throws RemoteException en cas d'erreur RMI
    * @throws SQLException en cas d'erreur SQL
    */
   List<String> getFactures() throws RemoteException, SQLException;

}
