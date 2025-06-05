package ServeurSiege;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServeurSiege {
    public static void main(String[] args) {
        try {
            // Lancer le registre RMI
            LocateRegistry.createRegistry(1100);

            // Créer l'instance du service
            ISiegeServeurImpl siegeServeur = new ISiegeServeurImpl();

            // Exporter l'objet et obtenir le stub
            ISiegeServeur skeleton = (ISiegeServeur) UnicastRemoteObject.exportObject(siegeServeur, 0);

            Registry registry = LocateRegistry.getRegistry(1100);
            registry.rebind("SiegeServeur", skeleton);

            System.out.println("✅ Serveur RMI lancé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur :");
            e.printStackTrace();
        }
    }
}