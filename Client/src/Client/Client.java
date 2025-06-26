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

    public Client() {
        try {
            Registry registry = LocateRegistry.getRegistry(1099);
            stub = (IBricoMerlinServices) registry.lookup("BricoMerlinService");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, " Erreur de connexion au serveur RMI : " + e.getMessage());
            System.exit(1);
        }

        setTitle("🛠️ BricoMerlin - Interface Client");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));

        JLabel title = new JLabel("Bienvenue chez BricoMerlin !");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(new Color(33, 94, 33));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(title, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(8, 1, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

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

        buttons[0].addActionListener(e -> consulterArticle());
        buttons[1].addActionListener(e -> consulterFamille());
        buttons[2].addActionListener(e -> acheterArticle());
        buttons[3].addActionListener(e -> ajouterStock());
        buttons[4].addActionListener(e -> payerFacture());
        buttons[5].addActionListener(e -> consulterFacture());
        buttons[6].addActionListener(e -> calculerCA());
        buttons[7].addActionListener(e -> System.exit(0));
    }

    private void consulterArticle() {
        try {
            List<String> listeRefs = stub.getArticles();  // Méthode qui retourne toutes les refs
            if (listeRefs == null || listeRefs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article trouvé.");
                return;
            }

            String ref = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une référence d'article :",
                    "Consultation d'article",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    listeRefs.toArray(),
                    listeRefs.get(0));

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

    private void consulterFamille() {
        try {
            List<String> famillesList = stub.getFamilles();
            if (famillesList == null || famillesList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucune famille d'articles trouvée.");
                return;
            }

            String nom = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez une famille d'articles :",
                    "Choix de la famille",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    famillesList.toArray(),
                    famillesList.get(0));

            List<String> articles = stub.ConsulterFamille(nom);
            if (articles != null && !articles.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Articles de la famille « ").append(nom).append(" »\n");
                sb.append("——————————————————————————\n");

                for (int i = 0; i < articles.size(); i += 4) {
                    String ref = articles.get(i);
                    String famille = articles.get(i + 1);
                    String prix = articles.get(i + 2);
                    String stock = articles.get(i + 3);

                    sb.append(String.format("Réf: %-8s | Prix: %-6s € | Stock: %-4s\n", ref, prix, stock));
                }

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

    private void acheterArticle() {
        try {
            Map<String, Integer> stockDispo = stub.getArticlesAvecStock(); // méthode à créer côté serveur
            List<String> refList = new ArrayList<>(stockDispo.keySet());
            List<String> refs = new ArrayList<>();
            List<Integer> qtes = new ArrayList<>();

            boolean continuer = true;
            while (continuer) {
                String article = (String) JOptionPane.showInputDialog(this, "Choisissez un article :", "Articles disponibles",
                        JOptionPane.QUESTION_MESSAGE, null, refList.toArray(), refList.get(0));

                if (article == null) break;

                int stock = stockDispo.get(article);

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

                int index = refs.indexOf(article);
                if (index != -1) {
                    qtes.set(index, qtes.get(index) + qte);
                } else {
                    refs.add(article);
                    qtes.add(qte);
                }

                stockDispo.put(article, stock - qte);

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

    private void ajouterStock() {
        try {
            List<String> refs = stub.getArticles();
            if (refs == null || refs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucun article disponible.");
                return;
            }

            String ref = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez l'article à mettre à jour :",
                    "Ajout de stock",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    refs.toArray(),
                    refs.get(0)
            );

            if (ref == null) return;

            String qteStr = JOptionPane.showInputDialog(this, "Quantité à ajouter :");
            if (qteStr == null) return;

            int qte = Integer.parseInt(qteStr);
            if (qte <= 0) {
                JOptionPane.showMessageDialog(this, "❗ La quantité doit être positive.");
                return;
            }

            stub.AjouterStockArticle(ref, qte);
            JOptionPane.showMessageDialog(this, "✅ Stock ajouté avec succès.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Quantité invalide.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Erreur : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void payerFacture() {
        try {
            List<String> facturesNonPayees = stub.getFacturesNonPayees();
            if (facturesNonPayees == null || facturesNonPayees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune facture à payer.");
                return;
            }

            String id = (String) JOptionPane.showInputDialog(
                    this,
                    "Sélectionnez l'ID de la facture à payer :",
                    "Paiement de facture",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    facturesNonPayees.toArray(),
                    facturesNonPayees.get(0)
            );
            if (id == null) return;

            stub.PayerFacture(id);
            JOptionPane.showMessageDialog(this, "✅ Facture payée avec succès.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void consulterFacture() {
        try {
            List<String> listeFactures = stub.getFactures();
            if (listeFactures == null || listeFactures.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Aucune facture trouvée.");
                return;
            }

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

            List<String> contenu = stub.ConsulterFacture(id);
            if (contenu == null || contenu.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ Facture introuvable ou vide.");
                return;
            }

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

    private void calculerCA() {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

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

        if (option != JOptionPane.OK_OPTION) return;

        Date selectedDate = (Date) dateSpinner.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(selectedDate);

        try {
            float ca = stub.CalculerCA(date);
            JOptionPane.showMessageDialog(this, " Chiffre d'affaires calculé pour " + date + " : " + ca + "€");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Client gui = new Client();
            gui.setVisible(true);
        });
    }
}