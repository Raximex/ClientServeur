package Client;

import Serveur.IBricoMerlinServices;

import javax.swing.*;
import java.awt.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;

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
        String ref = JOptionPane.showInputDialog(this, "Entrer la référence de l'article :");
        try {
            String[] article = stub.ConsulterArticle(ref);
            if (article != null && article[1] != null) {
                JOptionPane.showMessageDialog(this, String.format("\u2705 Référence : %s\n\uD83D\uDCC4 Famille : %s\n\uD83D\uDCB2 Prix : %s €\n📦 Stock : %s",
                        article[1], article[2], article[3], article[4]));
            } else {
                JOptionPane.showMessageDialog(this, " Article non trouvé.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void consulterFamille() {
        String nom = JOptionPane.showInputDialog(this, "Nom de la famille d'articles :");
        try {
            String[] articles = stub.ConsulterFamille(nom);
            if (articles != null && articles.length > 1) {
                StringBuilder sb = new StringBuilder(" Articles dans la famille :\n\n");
                for (String s : articles) {
                    if (s != null) sb.append(s).append("\n");
                }
                JOptionPane.showMessageDialog(this, sb.toString());
            } else {
                JOptionPane.showMessageDialog(this, " Aucun article trouvé pour cette famille.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void acheterArticle() {
        try {
            String ref = JOptionPane.showInputDialog(this, "Référence de l'article :");
            int qte = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantité à acheter :"));
            stub.AcheterArticle(ref, qte);
            JOptionPane.showMessageDialog(this, " Achat effectué avec succès.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void ajouterStock() {
        try {
            String ref = JOptionPane.showInputDialog(this, "Référence de l'article :");
            int qte = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantité à ajouter :"));
            stub.AjouterStockArticle(ref, qte);
            JOptionPane.showMessageDialog(this, " Stock ajouté avec succès.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void payerFacture() {
        String id = JOptionPane.showInputDialog(this, "ID de la facture à payer :");
        try {
            stub.PayerFacture(id);
            JOptionPane.showMessageDialog(this, " Facture payée.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void consulterFacture() {
        String id = JOptionPane.showInputDialog(this, "ID de la facture :");
        try {
            String facture = stub.ConsulterFacture(id);
            JOptionPane.showMessageDialog(this, facture != null ? facture : " Facture non trouvée.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, " Erreur : " + ex.getMessage());
        }
    }

    private void calculerCA() {
        String date = JOptionPane.showInputDialog(this, "Entrer la date (yyyy-MM-dd) :");
        try {
            stub.CalculerCA(date);
            JOptionPane.showMessageDialog(this, " Chiffre d'affaires calculé pour " + date);
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