package ServeurSiege;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Serveur RMI du siège.
 */
public class ServeurSiege {

    /**
     * Point d'entrée du serveur RMI siege de BricoMerlin.
     * Initialise le registre RMI et enregistre le service du server siege.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     * @throws RemoteException en cas d'erreur RMI
     */
    public static void main(String[] args) throws RemoteException {
        try {
            // Lancer le registre RMI sur le port 1100
            Registry registry = LocateRegistry.createRegistry(1100);

            // Créer l'instance du service métier
            ISiegeServeurImpl siegeServeur = new ISiegeServeurImpl();

            // Exporter l'objet en tant qu'objet distant
            ISiegeServeur skeleton = (ISiegeServeur)
                    UnicastRemoteObject.exportObject(siegeServeur, 0);

            // Publier l’objet dans le registre RMI
            registry.rebind("SiegeServeur", skeleton);

            System.out.println("✅ Serveur RMI lancé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur :");
            e.printStackTrace();
        }
    }
}
