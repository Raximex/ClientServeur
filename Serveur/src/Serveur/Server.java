package Serveur;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    public static void main(String[] args) {
        try {
            // Lancer le registre RMI si pas déjà lancé
            LocateRegistry.createRegistry(1099);

            BricoMerlinServicesImpl bricoMerlinServices = new BricoMerlinServicesImpl();
            String url = "rmi://" + InetAddress.getLocalHost().getHostAddress() + "/TestRMI";
            System.out.println("Enregistrement de l'objet avec l'url : " + url);
            Naming.rebind(url, bricoMerlinServices);
            System.out.println("✅ Serveur RMI lancé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur :");
            e.printStackTrace();
        }
    }
}
