package Serveur;

import ServeurSiege.ISiegeServeur;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Serveur du magasin.
 */
public class Server {
    private ISiegeServeur stub;
    /**
     * Constructeur du serveur magasin.
     * Établit la connexion RMI avec le SiegeServeur et lance la mise à jour des prix au lancement.
     *
     * @throws RemoteException si la connexion RMI échoue
     * @throws SQLException si une erreur SQL survient lors de la mise à jour des prix
     */
    public Server() throws RemoteException, SQLException {
        try {
            Registry registry = LocateRegistry.getRegistry(1100);
            stub = (ISiegeServeur) registry.lookup("SiegeServeur");
            System.out.println("Connecté au SiegeServeur");
        } catch (Exception e) {
            // Rejette toute exception en tant que RemoteException pour uniformité côté client
            throw new RemoteException(e.getMessage());
        }
        // Mise à jour des prix au démarrage du serveur
        miseAjourPrix();
    }

    /**
     * Met à jour les prix du serveur en appelant une fonction du serveur Siege.
     * MiseAJourServeur est la fonction qui met à jour la base du serveur.
     * MiseAJourPrix est la fonction qui renvoi une hashmap clé valeur contenant les nouveaux prix des articles.
     * @throws RemoteException
     * @throws SQLException
     */
    public void miseAjourPrix() throws RemoteException, SQLException {
        BricoMerlinServicesImpl.miseAJourServeur(stub.miseAJourPrix());
    }

    /**
     * Envoi les factures de la journée au serveur Siege.
     * @throws RemoteException
     */
    public void sendFactures() throws RemoteException {
        File folder = new File("Serveur/Factures");
        File[] files = folder.listFiles();

        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains("facture_" + date)) {
                    try (FileInputStream in = new FileInputStream(file)) {
                        byte[] data = new byte[(int) file.length()];
                        in.read(data);
                        stub.getFactures(file.getName(), data);
                        System.out.println("Fichier envoyé : " + file.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Aucun fichier trouvé dans le dossier 'Factures'.");
        }
    }

    /**
     * Permet d'appeler certaines fonctions à un temps donné
     * @param hour
     * @param minute
     * @param server
     * @param action sert à savoir si on veut appeler la fonction pour la mise a jour des prix ou l'envoi des factures (ces deux actions sont faites a des temps T différends).
     */
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
                            server.miseAjourPrix();
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

    /**
     * Point d'entrée du serveur RMI BricoMerlin.
     * Initialise le registre RMI, enregistre le service et lance les tâches planifiées.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     * @throws SQLException en cas d'erreur lors de la création du serveur
     * @throws RemoteException en cas d'erreur RMI
     */
    public static void main(String[] args) throws SQLException, RemoteException {
        try {
            // Création du registre RMI sur le port 1099
            LocateRegistry.createRegistry(1099);

            // Instanciation de l'implémentation du service
            BricoMerlinServicesImpl bricoMerlinServices = new BricoMerlinServicesImpl();

            // Export du service et obtention du stub RMI
            IBricoMerlinServices skeleton = (IBricoMerlinServices) UnicastRemoteObject.exportObject(bricoMerlinServices, 0);

            // Liaison du stub dans le registre avec le nom "BricoMerlinService"
            Registry registry = LocateRegistry.getRegistry(1099);
            registry.rebind("BricoMerlinService", skeleton);

            System.out.println("✅ Serveur RMI lancé avec succès !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du démarrage du serveur :");
            e.printStackTrace();
            // En cas d'erreur critique, on peut décider d'arrêter l'application ici
            System.exit(1);
        }

        // Création de l'instance Server (qui fait la connexion au SiegeServeur, etc.)
        Server server = new Server();

        // Planification des tâches journalières (mise à jour prix, envoi factures)
        scheduleDailyTask(7, 0, server, true);
        scheduleDailyTask(23, 59, server, false);
    }

}