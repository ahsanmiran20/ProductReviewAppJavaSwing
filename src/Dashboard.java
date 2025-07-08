import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {
    public Dashboard() {
        setTitle("Product Review App - Dashboard");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with Close button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("X");
        btnClose.setFocusPainted(false);
        btnClose.setForeground(Color.RED);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setPreferredSize(new Dimension(40, 28));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        topPanel.add(btnClose);
        add(topPanel, BorderLayout.NORTH);

        // Add DashboardPanel (buttons panel) in center
        add(new DashboardPanel(), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }
}
