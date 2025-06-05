package Serveur;

import ServeurSiege.ISiegeServeur;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

public class Server {
    private ISiegeServeur stub;
    public Server() throws RemoteException, SQLException {
        try {
            Registry registry = LocateRegistry.getRegistry(1100);
            stub = (ISiegeServeur) registry.lookup("SiegeServeur");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }


        stub.miseAJourPrix();
    }

    public static void main(String[] args) throws SQLException, RemoteException {
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
        Server server = new Server();
    }
}