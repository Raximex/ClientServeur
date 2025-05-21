package Serveur;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public static void main(String[] args) {
        try {
            // Lancer le registre RMI
            LocateRegistry.createRegistry(1099);

            // Créer l'instance du service
            BricoMerlinServicesImpl bricoMerlinServices = new BricoMerlinServicesImpl();

            // Exporter l'objet et obtenir le stub
            IBricoMerlinServices skeleton = (IBricoMerlinServices) UnicastRemoteObject.exportObject(bricoMerlinServices, 0);
            //System.out.println("Enregistrement de l'objet avec l'url : " + url);
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind("BricoMerlinService", skeleton);

            System.out.println("✅ Serveur RMI lancé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur :");
            e.printStackTrace();
        }
    }
}