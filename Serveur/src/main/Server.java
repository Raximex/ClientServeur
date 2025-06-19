package Serveur;

import ServeurSiege.ISiegeServeur;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.HashMap;
import java.util.TimerTask;

public class Server {
    private ISiegeServeur stub;
    public Server() throws RemoteException, SQLException {
        try {
            Registry registry = LocateRegistry.getRegistry(1100);
            stub = (ISiegeServeur) registry.lookup("SiegeServeur");
            System.out.println("Connecté au SiegeServeur");
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }


        MiseAjourPrix();// mise à jour du prix lors du lancement du serveur
    }



    public void MiseAjourPrix() throws RemoteException, SQLException {
        BricoMerlinServicesImpl.MiseAJourServeur(stub.miseAJourPrix());
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


        scheduleDailyTask(7, 0,server); // Heure = 7h00

    }

    public static void scheduleDailyTask(int hour, int minute, Server server) {
        Timer timer = new Timer();

        // Définir la première exécution à demain 7h00 si l'heure est déjà passée
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Date firstRun = calendar.getTime();

        // Si l'heure est déjà passée aujourd'hui, programmer pour demain
        if (firstRun.before(new Date())) {
            calendar.add(Calendar.DATE, 1);
            firstRun = calendar.getTime();
        }

        long period = 24 * 60 * 60 * 1000; // 24 heures en millisecondes

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    server.MiseAjourPrix();
                } catch (RemoteException | SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, firstRun, period);
    }
}