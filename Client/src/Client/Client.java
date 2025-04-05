package Client;
import Serveur.IBricoMerlinServices;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            // Connexion au registre RMI sur localhost, port 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            // Récupération de l'objet distant sous le nom "BricoMerlinService"
            IBricoMerlinServices stub = (IBricoMerlinServices) registry.lookup("BricoMerlinService");

            Scanner scanner = new Scanner(System.in);
            int choix = 0;

            while (true) {
                System.out.println("=====================================");
                System.out.println("Menu Brico-Merlin");
                System.out.println("1. Consulter un article (par référence)");
                System.out.println("2. Consulter une famille d'articles");
                System.out.println("3. Acheter un article");
                System.out.println("4. Ajouter du stock à un article");
                System.out.println("5. Payer une facture");
                System.out.println("6. Consulter une facture");
                System.out.println("7. Calculer le chiffre d'affaires pour une date");
                System.out.println("8. Quitter");
                System.out.print("Votre choix : ");
                choix = scanner.nextInt();
                scanner.nextLine(); // Consommation du retour à la ligne

                switch (choix) {
                    case 1:
                        // Consulter un article
                        System.out.print("Entrez la référence de l'article : ");
                        String refArticle = scanner.nextLine();
                        try {
                            String[] article = stub.ConsulterArticle(refArticle);
                            if (article != null && article[0] != null) {
                                System.out.println("Détails de l'article :");
                                System.out.println("Référence     : " + article[0]);
                                System.out.println("Famille       : " + article[1]);
                                System.out.println("Prix unitaire : " + article[2]);
                                System.out.println("Stock total   : " + article[3]);
                            } else {
                                System.out.println("Article non trouvé.");
                            }
                        } catch (RemoteException | SQLException e) {
                            System.out.println("Erreur lors de la consultation de l'article : " + e.getMessage());
                        }
                        break;

                    case 2:
                        // Consulter une famille d'articles
                        System.out.print("Entrez le nom de la famille d'articles : ");
                        String famille = scanner.nextLine();
                        try {
                            // Supposons que la méthode renvoie un tableau de chaînes avec les articles
                            String[] articlesFamille = stub.ConsulterFamille(famille);
                            if (articlesFamille != null) {
                                System.out.println("Articles de la famille " + famille + " :");
                                // Parcourir le tableau et afficher les entrées non nulles
                                for (int i = 0; i < articlesFamille.length; i++) {
                                    if (articlesFamille[i] != null) {
                                        System.out.println(articlesFamille[i]);
                                    }
                                }
                            } else {
                                System.out.println("Aucun article trouvé pour cette famille.");
                            }
                        } catch (RemoteException | SQLException e) {
                            System.out.println("Erreur lors de la consultation de la famille : " + e.getMessage());
                        }
                        break;

                    case 3:
                        // Acheter un article
                        System.out.print("Entrez la référence de l'article à acheter : ");
                        String refAchat = scanner.nextLine();
                        System.out.print("Entrez la quantité à acheter : ");
                        int quantite = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            stub.AcheterArticle(refAchat, quantite);
                            System.out.println("Achat effectué avec succès.");
                        } catch (RemoteException | SQLException e) {
                            System.out.println("Erreur lors de l'achat de l'article : " + e.getMessage());
                        }
                        break;

                    case 4:
                        // Ajouter du stock à un article
                        System.out.print("Entrez la référence de l'article : ");
                        String refStock = scanner.nextLine();
                        System.out.print("Entrez la quantité à ajouter : ");
                        int qteAjout = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            stub.AjouterStockArticle(refStock, qteAjout);
                            System.out.println("Stock ajouté avec succès.");
                        } catch (RemoteException e) {
                            System.out.println("Erreur lors de l'ajout du stock : " + e.getMessage());
                        }
                        break;

                    case 5:
                        // Payer une facture
                        System.out.print("Entrez l'ID de la facture à payer : ");
                        String idFacture = scanner.nextLine();
                        try {
                            stub.PayerFacture(idFacture);
                            System.out.println("Facture payée avec succès.");
                        } catch (RemoteException e) {
                            System.out.println("Erreur lors du paiement de la facture : " + e.getMessage());
                        }
                        break;

                    case 6:
                        // Consulter une facture
                        System.out.print("Entrez l'ID de la facture à consulter : ");
                        String idConsulter = scanner.nextLine();
                        try {
                            String facture = stub.ConsulterFacture(idConsulter);
                            if (facture != null) {
                                System.out.println("Détails de la facture :");
                                System.out.println(facture);
                            } else {
                                System.out.println("Facture non trouvée.");
                            }
                        } catch (RemoteException e) {
                            System.out.println("Erreur lors de la consultation de la facture : " + e.getMessage());
                        }
                        break;

                    case 7:
                        // Calculer le chiffre d'affaires pour une date donnée
                        System.out.print("Entrez la date (format yyyy-MM-dd) : ");
                        String date = scanner.nextLine();
                        try {
                            stub.CalculerCA(date);
                            System.out.println("Chiffre d'affaires calculé pour la date " + date + ".");
                        } catch (RemoteException e) {
                            System.out.println("Erreur lors du calcul du CA : " + e.getMessage());
                        }
                        break;

                    case 8:
                        System.out.println("Au revoir !");
                        scanner.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Choix invalide. Veuillez réessayer.");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
