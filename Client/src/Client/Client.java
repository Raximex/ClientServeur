package Client;
import Serveur.IBricoMerlinServices;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {
    
    private Client(){}

    public static void main(String[] args) {
        try {  
            // Récupérer le registre
            Registry reg = LocateRegistry.getRegistry(null); 
       
            // Recherche dans le registre de l'objet distant
            IBricoMerlinServices stub = (IBricoMerlinServices) reg.lookup("IBricoMerlinServices");
       
            // Front client pour lancer les appels des méthodes distantes à l'aide de l'objet obtenu
            //stub. 
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
                scanner.nextLine(); // Consommer le retour à la ligne

                switch (choix) {
                    case 1:
                        System.out.print("Entrez la référence de l'article : ");
                        String refArticle = scanner.nextLine();
                        try {
                            String[] article = stub.ConsulterArticle(refArticle);
                            if (article != null && article[0] != null) {
                                System.out.println("Détails de l'article :");
                                System.out.println("Référence       : " + article[0]);
                                System.out.println("Famille         : " + article[1]);
                                System.out.println("Prix unitaire   : " + article[2]);
                                System.out.println("Nombre total    : " + article[3]);
                            } else {
                                System.out.println("Article non trouvé.");
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la consultation de l'article : " + e.getMessage());
                        }
                        break;

                    case 2:
                        System.out.print("Entrez la famille d'articles à consulter : ");
                        String famille = scanner.nextLine();
                        try {
                            stub.ConsulterFamille(famille);
                            // Ici, la méthode étant void, il faudra modifier l'interface pour renvoyer les résultats.
                            System.out.println("Consultation de la famille effectuée.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la consultation de la famille : " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.print("Entrez la référence de l'article à acheter : ");
                        String refAchat = scanner.nextLine();
                        System.out.print("Entrez la quantité à acheter : ");
                        int qteAchat = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            stub.AcheterArticle(refAchat, qteAchat);
                            System.out.println("Achat effectué avec succès.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors de l'achat : " + e.getMessage());
                        }
                        break;

                    case 4:
                        System.out.print("Entrez la référence de l'article pour ajouter du stock : ");
                        String refStock = scanner.nextLine();
                        System.out.print("Entrez la quantité à ajouter : ");
                        int qteAjout = scanner.nextInt();
                        scanner.nextLine();
                        try {
                            stub.AjouterStockArticle(refStock, qteAjout);
                            System.out.println("Stock ajouté avec succès.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors de l'ajout de stock : " + e.getMessage());
                        }
                        break;

                    case 5:
                        System.out.print("Entrez l'ID de la facture à payer : ");
                        String idFacture = scanner.nextLine();
                        try {
                            stub.PayerFacture(idFacture);
                            System.out.println("Facture payée avec succès.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors du paiement de la facture : " + e.getMessage());
                        }
                        break;

                    case 6:
                        System.out.print("Entrez l'ID de la facture à consulter : ");
                        String idConsulter = scanner.nextLine();
                        try {
                            stub.ConsulterFacture(idConsulter);
                            // La méthode étant void, pensez à modifier l'interface pour retourner la facture.
                            System.out.println("Consultation de la facture effectuée.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la consultation de la facture : " + e.getMessage());
                        }
                        break;

                    case 7:
                        System.out.print("Entrez la date pour le calcul du CA (format yyyy-MM-dd) : ");
                        String date = scanner.nextLine();
                        try {
                            stub.CalculerCA(date);
                            // Comme précédemment, pour afficher un résultat, il faudrait que la méthode retourne un chiffre.
                            System.out.println("Calcul du chiffre d'affaires effectué.");
                        } catch (Exception e) {
                            System.out.println("Erreur lors du calcul du CA : " + e.getMessage());
                        }
                        break;

                    case 8:
                        System.out.println("Au revoir !");
                        scanner.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Choix invalide, veuillez réessayer.");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}