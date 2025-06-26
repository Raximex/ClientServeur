package ServeurSiege;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Interface RMI pour le service du siège.
 * Définit les opérations distantes de gestion des prix et de collecte de factures.
 */
public interface ISiegeServeur extends Remote {

   /**
    * Met à jour les prix unitaires des articles sur les serveurs magasins.
    *
    * @return une map associant chaque référence d'article à son nouveau prix
    * @throws RemoteException en cas d’erreur de communication RMI
    * @throws SQLException en cas d’erreur lors de l’accès à la base de données
    */
   HashMap<String, Float> miseAJourPrix() throws RemoteException, SQLException;

   /**
    * Reçoit les factures provenant d’un serveur magasin.
    *
    * @param filename nom du fichier de factures
    * @param data     contenu binaire du fichier
    * @throws RemoteException en cas d’erreur de communication RMI
    * @throws IOException en cas d’erreur de lecture/écriture de fichier
    */
   void getFactures(String filename, byte[] data) throws RemoteException, IOException;
}
