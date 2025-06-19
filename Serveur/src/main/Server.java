package Serveur;

import ServeurSiege.ISiegeServeur;

import java.io.*;
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
        sendFactures();//envoie test
    }

    public void MiseAjourPrix() throws RemoteException, SQLException {
        BricoMerlinServicesImpl.MiseAJourServeur(stub.miseAJourPrix());
    }

    public void sendFactures() throws RemoteException {
        File folder = new File("Serveur/Factures");
        File[] files = folder.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    byte[] data = new byte[(int) file.length()];
                    try {
                        FileInputStream in = new FileInputStream(file);
                        in.read(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stub.getFactures(file.getName(), data);
                    System.out.println("Fichier envoyé : " + file.getName());
                }
            }
        } else {
            System.out.println("Aucun fichier trouvé dans le dossier 'factures'.");
        }
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

        scheduleDailyTask(7, 0, server, true); // Heure = 7h00
        scheduleDailyTask(23, 59, server, false);
    }

    //Si action = true => maj prix matin, si action = false => envoie factures soir.
    public static void scheduleDailyTask(int hour, int minute, Server server, boolean action) {
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

            timer.scheduleAtFixedRate( new TimerTask() {
                @Override
                public void run() {
                    if (action == true) {
                        try {
                            server.MiseAjourPrix();
                        } catch (RemoteException | SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        try {
                            server.sendFactures();
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, firstRun, period);
        }
    }