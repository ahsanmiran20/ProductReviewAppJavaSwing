import javax.swing.*;
import java.awt.*;

public class WelcomePage extends JFrame {
    public WelcomePage(String userEmail) {
        setTitle("Welcome - Product Review App");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel for Profile and Logout buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton profileBtn = new JButton("Profile");
        JButton logoutBtn = new JButton("Logout");

        topPanel.add(profileBtn);
        topPanel.add(logoutBtn);

        add(topPanel, BorderLayout.NORTH);

        // Center panel - DashboardPanel
        DashboardPanel dashboardPanel = new DashboardPanel();
        add(dashboardPanel, BorderLayout.CENTER);

        // Logout action
        logoutBtn.addActionListener(e -> {
            new Login().setVisible(true); // Assuming you have Login class
            dispose();
        });

        // Profile button action (add your own logic here)
        profileBtn.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Profile page coming soon!",
                "Profile",
                JOptionPane.INFORMATION_MESSAGE));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WelcomePage("test@example.com").setVisible(true));
    }
}
