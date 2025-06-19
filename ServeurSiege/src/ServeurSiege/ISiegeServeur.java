package ServeurSiege;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;

// Créer l'interface de l'objet distant
public interface ISiegeServeur extends Remote {


   //mise a jour des prix des références envoyés aux serveurs.
   HashMap<String,Float> miseAJourPrix() throws RemoteException, SQLException;

}