import javax.swing.*;
import java.awt.*;

public class ProductReviewApp extends JFrame {

    public ProductReviewApp(String userEmail) {
        setTitle("Product Review App - Dashboard");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with Profile and Logout on the right
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProfile = new JButton("Profile");
        JButton btnLogout = new JButton("Logout");

        btnLogout.addActionListener(e -> {
            dispose(); // Close current window
            new Login().setVisible(true); // Go back to login
        });

        rightPanel.add(btnProfile);
        rightPanel.add(btnLogout);
        topPanel.add(rightPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center panel - this is your Dashboard content
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new GridLayout(3, 2, 20, 20));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] modules = {"Products", "Categories", "Users", "Orders", "Reviews", "Reports"};
        for (String module : modules) {
            JButton btn = new JButton(module);
            btn.setPreferredSize(new Dimension(120, 60));
            dashboardPanel.add(btn);

            // Add action listener for each button if needed
            btn.addActionListener(ev -> {
                JOptionPane.showMessageDialog(this, module + " module clicked!");
                // Here you can open the corresponding module window
            });
        }

        add(dashboardPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ProductReviewApp("test@example.com").setVisible(true);
        });
    }
}
