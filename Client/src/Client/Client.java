package Client;

import Serveur.IBricoMerlinServices;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Client extends JFrame {
    private IBricoMerlinServices stub;

    /**
     * Constructeur de l'interface client de l'application BricoMerlin.
     * Il établit la connexion RMI avec le serveur distant, initialise l'interface
     * graphique principale, configure les boutons d'action et gère les erreurs de
     * connexion.
     */
    public Client() {
        try {
            // Connexion au registre RMI sur le port 1099 et récupération du stub distant
            Registry registry = LocateRegistry.getRegistry(1099);
            stub = (IBricoMerlinServices) registry.lookup("BricoMerlinService");
        } catch (Exception e) {
            // Affichage d'une boîte de dialogue en cas d'échec de la connexion RMI
            JOptionPane.showMessageDialog(this, " Erreur de connexion au serveur RMI : " + e.getMessage());
            System.exit(1);
        }

        // Configuration de la fenêtre principale
        setTitle("🛠️ BricoMerlin - Interface Client");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Création du panneau principal avec gestion de layout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));

        // Titre principal
        JLabel title = new JLabel("Bienvenue chez BricoMerlin !");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(33, 94, 33));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(title, gbc);

        // Création du panneau de boutons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(8, 1, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        // Liste des actions disponibles pour l'utilisateur
        String[] actions = {
                " Consulter un article",
                " Consulter une famille d'articles",
                " Acheter un article",
                " Ajouter du stock",
                " Payer une facture",
                " Consulter une facture",
                " Calculer le chiffre d'affaires",
                " Quitter"
        };

        // Création et ajout des boutons à l'interface
        JButton[] buttons = new JButton[actions.length];
        for (int i = 0; i < actions.length; i++) {
            buttons[i] = new JButton(actions[i]);
            buttons[i].setFocusPainted(false);
            buttons[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            buttonPanel.add(buttons[i]);
        }
        gbc.gridy = 1;
        panel.add(buttonPanel, gbc);
        add(panel);

        // Association des boutons à leurs actions respectives
        buttons[0].addActionListener(e -> consulterArticle());
        buttons[1].addActionListener(e -> consulterFamille());
        buttons[2].addActionListener(e -> acheterArticle());
        buttons[3].addActionListener(e -> ajouterStock());
        buttons[4].addActionListener(e -> payerFacture());
        buttons[5].addActionListener(e -> consulterFacture());
        buttons[6].addActionListener(e -> calculerCA());
        buttons[7].addActionListener(e -> System.exit(0));
    }

    /**
     * Affiche les détails d’un article sélectionné depuis la liste des références disponibles.
     * Affiche un message d’erreur si aucun article n’est trouvé ou en cas d’échec de la communication.
     */
    private void consulterArticle() {
        try {
            // Récupération de toutes les références d'articles disponibles depuis le serveur
            List<String> listeRefs = stub.getArticles();
            if (listeRefs == null || listeRefs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article trouvé.");
                return;
            }

            // Affichage d'une boîte de sélection pour choisir une référence
            String ref = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une référence d'article :",
                    "Consultation d'article",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    listeRefs.toArray(),
                    listeRefs.get(0) // Valeur par défaut
            );

            // Si l'utilisateur annule la sélection
            if (ref == null) return;

            // Récupération des détails de l'article
            List<String> article = stub.ConsulterArticle(ref);
            if (article != null && article.size() >= 4) {
                StringBuilder sb = new StringBuilder();
                sb.append("Fiche article\n\n");
                sb.append("——————————————————————————\n");
                sb.append("Référence       : \t").append(article.get(0)).append("\n");
                sb.append("Famille         : \t").append(article.get(1)).append("\n");
                sb.append("Prix unitaire   : \t").append(article.get(2)).append(" €\n");
                sb.append("Stock disponible: \t").append(article.get(3)).append("\n");

                JOptionPane.showMessageDialog(this, sb.toString(), "Détails de l'article", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Article non trouvé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❗ Erreur lors de la consultation : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Affiche la liste des articles d’une famille sélectionnée par l'utilisateur.
     * Affiche un message d'erreur en cas d'absence de familles, d’articles, ou en cas d’erreur de communication.
     */
    private void consulterFamille() {
        try {
            // Récupération des familles d'articles disponibles depuis le serveur
            List<String> famillesList = stub.getFamilles();
            if (famillesList == null || famillesList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucune famille d'articles trouvée.");
                return;
            }

            // Boîte de dialogue pour choisir une famille
            String nom = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une famille d'articles :",
                    "Choix de la famille",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    famillesList.toArray(),
                    famillesList.get(0)
            );

            // Si l'utilisateur annule
            if (nom == null) return;

            // Récupération des articles de la famille choisie
            List<String> articles = stub.ConsulterFamille(nom);
            if (articles != null && !articles.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Articles de la famille « ").append(nom).append(" »\n");
                sb.append("——————————————————————————\n");

                // Les articles sont transmis par blocs de 4 infos (ref, famille, prix, stock)
                for (int i = 0; i <= articles.size() - 4; i += 4) {
                    String ref = articles.get(i);
                    String famille = articles.get(i + 1);  // Pas utilisé ici, mais cohérent pour structure
                    String prix = articles.get(i + 2);
                    String stock = articles.get(i + 3);

                    sb.append(String.format("Réf: %-8s | Prix: %-6s € | Stock: %-4s\n", ref, prix, stock));
                }

                // Affichage dans une zone scrollable avec police monospace pour l’alignement
                JTextArea area = new JTextArea(sb.toString(), 15, 50);
                area.setEditable(false);
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Articles de la famille", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "❌ Aucun article trouvé pour la famille \"" + nom + "\".");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❗ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Permet à l'utilisateur de sélectionner un ou plusieurs articles à acheter,
     * de saisir les quantités souhaitées, et de valider ou non le paiement immédiat.
     * Les articles sont choisis parmi ceux ayant du stock disponible.
     * Affiche des messages d'erreur en cas de saisie invalide ou de problème serveur.
     */
    private void acheterArticle() {
        try {
            // Récupère la liste des articles disponibles avec leur stock (à implémenter côté serveur)
            Map<String, Integer> stockDispo = stub.getArticlesAvecStock();
            List<String> refList = new ArrayList<>(stockDispo.keySet());
            if (refList == null || refList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article trouvé.");
                return;
            }

            // Références et quantités sélectionnées par l'utilisateur
            List<String> refs = new ArrayList<>();
            List<Integer> qtes = new ArrayList<>();

            boolean continuer = true;
            while (continuer) {
                // Sélection d’un article
                String article = (String) JOptionPane.showInputDialog(
                        this,
                        "Choisissez un article :",
                        "Articles disponibles",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        refList.toArray(),
                        refList.get(0)
                );

                // Annulation de la sélection
                if (article == null) break;

                int stock = stockDispo.get(article);

                // Saisie de la quantité
                String qteStr = JOptionPane.showInputDialog(this, "Quantité à acheter (stock dispo : " + stock + ") :");
                if (qteStr == null) continue;

                int qte;
                try {
                    qte = Integer.parseInt(qteStr);
                    if (qte <= 0) {
                        JOptionPane.showMessageDialog(this, "Quantité invalide.");
                        continue;
                    }
                    if (qte > stock) {
                        JOptionPane.showMessageDialog(this, "Stock insuffisant pour cet article.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Quantité non valide.");
                    continue;
                }

                // Si l’article est déjà dans la sélection, on cumule les quantités
                int index = refs.indexOf(article);
                if (index != -1) {
                    qtes.set(index, qtes.get(index) + qte);
                } else {
                    refs.add(article);
                    qtes.add(qte);
                }

                // Mise à jour temporaire du stock côté client pour éviter dépassement
                stockDispo.put(article, stock - qte);

                // Confirmation de continuer ou de payer
                int reponse = JOptionPane.showConfirmDialog(this, "Voulez-vous acheter d'autres articles ?", "Continuer", JOptionPane.YES_NO_OPTION);
                if (reponse != JOptionPane.YES_OPTION) {
                    boolean payer = JOptionPane.showConfirmDialog(this, "Voulez-vous payer maintenant ?") == JOptionPane.YES_OPTION;
                    stub.AcheterArticle(refs, qtes, payer);
                    continuer = false;
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Permet à l'utilisateur d'ajouter une quantité de stock à un article sélectionné.
     * Affiche un message de confirmation ou d'erreur selon le résultat.
     */
    private void ajouterStock() {
        try {
            // Récupère la liste des articles disponibles
            List<String> refs = stub.getArticles();
            if (refs == null || refs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article disponible.");
                return;
            }

            // Sélection de l'article
            String ref = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez l'article à mettre à jour :",
                    "Ajout de stock",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    refs.toArray(),
                    refs.get(0)
            );

            // Annulation de la sélection
            if (ref == null) return;

            // Saisie de la quantité à ajouter
            String qteStr = JOptionPane.showInputDialog(this, "Quantité à ajouter :");
            if (qteStr == null) return;

            int qte = Integer.parseInt(qteStr);
            if (qte <= 0) {
                JOptionPane.showMessageDialog(this, "❗ La quantité doit être positive.");
                return;
            }

            // Mise à jour du stock côté serveur
            stub.AjouterStockArticle(ref, qte);
            JOptionPane.showMessageDialog(this, "✅ Stock ajouté avec succès.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Quantité invalide.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Permet à l'utilisateur de payer une facture sélectionnée parmi les factures non réglées.
     * Affiche une confirmation ou un message d'erreur en fonction du résultat.
     */
    private void payerFacture() {
        try {
            // Récupère la liste des factures non payées
            List<String> facturesNonPayees = stub.getFacturesNonPayees();
            if (facturesNonPayees == null || facturesNonPayees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune facture à payer.");
                return;
            }

            // Sélection de l'ID de facture
            String id = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez l'ID de la facture à payer :",
                    "Paiement de facture",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    facturesNonPayees.toArray(),
                    facturesNonPayees.get(0)
            );

            // Annulation de l'action
            if (id == null) return;

            // Paiement via le serveur
            stub.PayerFacture(id);
            JOptionPane.showMessageDialog(this, "✅ Facture payée avec succès.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Permet à l'utilisateur de consulter le détail d'une facture sélectionnée.
     * Affiche la facture ou un message d'erreur si elle est vide/introuvable.
     */
    private void consulterFacture() {
        try {
            // Récupère la liste des factures existantes
            List<String> listeFactures = stub.getFactures();
            if (listeFactures == null || listeFactures.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucune facture trouvée.");
                return;
            }

            // Sélection de la facture à consulter
            String id = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une facture à consulter :",
                    "Consultation de facture",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    listeFactures.toArray(),
                    listeFactures.get(0)
            );
            if (id == null) return;

            // Récupération du contenu de la facture
            List<String> contenu = stub.ConsulterFacture(id);
            if (contenu == null || contenu.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Facture introuvable ou vide.");
                return;
            }

            // Construction de l'affichage ligne par ligne
            StringBuilder sb = new StringBuilder("Facture ").append(id).append(" :\n\n");
            for (String ligne : contenu) {
                if (ligne != null && !ligne.trim().isEmpty()) {
                    sb.append(ligne).append("\n");
                }
            }

            JOptionPane.showMessageDialog(this, sb.toString(), "Détail de la facture", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❗ Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Demande une date à l'utilisateur et affiche le chiffre d'affaires correspondant.
     * Affiche des messages d'erreur en cas de saisie invalide ou de problème serveur.
     */
    private void calculerCA() {
        // Création du sélecteur de date (spinner formaté)
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

        // Affichage de la boîte de dialogue avec le sélecteur
        int option = JOptionPane.showOptionDialog(
                this,
                dateSpinner,
                "Sélectionnez une date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        );

        // Annulation par l'utilisateur
        if (option != JOptionPane.OK_OPTION) return;

        // Conversion de la date sélectionnée au format attendu
        Date selectedDate = (Date) dateSpinner.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(selectedDate);

        try {
            // Appel distant pour le calcul du chiffre d'affaires
            float ca = stub.CalculerCA(date);
            JOptionPane.showMessageDialog(this, " Chiffre d'affaires calculé pour " + date + " : " + ca + "€");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    /**
     * Point d'entrée de l'application client.
     * Initialise et affiche l'interface graphique Swing dans le thread dédié.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client gui = new Client();
            gui.setVisible(true);
        });
    }

}