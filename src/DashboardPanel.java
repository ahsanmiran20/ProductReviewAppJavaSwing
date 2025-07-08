import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;

        String[] btnLabels = {"Products", "Categories", "Users", "Orders", "Reviews", "Reports"};
        JButton[] buttons = new JButton[btnLabels.length];

        Font btnFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color btnBg = new Color(30, 144, 255);
        Color btnFg = Color.WHITE;

        for (int i = 0; i < btnLabels.length; i++) {
            buttons[i] = new JButton(btnLabels[i]);
            buttons[i].setFont(btnFont);
            buttons[i].setBackground(btnBg);
            buttons[i].setForeground(btnFg);
            buttons[i].setFocusPainted(false);
            buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            buttons[i].setPreferredSize(new Dimension(270, 38));  // smaller but nice size
            buttons[i].setOpaque(true);
            buttons[i].setBorderPainted(false);

            // Rounded corners UI
            buttons[i].setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                protected void paintBackground(Graphics g, AbstractButton b) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(b.getModel().isPressed() ? btnBg.darker() : btnBg);
                    g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 15, 15);
                }
            });

            // Hover effect
            buttons[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    evt.getComponent().setBackground(btnBg.brighter());
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    evt.getComponent().setBackground(btnBg);
                }
            });

            gbc.gridy = i;
            add(buttons[i], gbc);
        }

        // Button actions
        buttons[0].addActionListener(e -> new Products().setVisible(true));
        buttons[1].addActionListener(e -> new Categories().setVisible(true));
        buttons[2].addActionListener(e -> new Users().setVisible(true));
        buttons[3].addActionListener(e -> new Orders().setVisible(true));
        buttons[4].addActionListener(e -> new Reviews().setVisible(true));
        buttons[5].addActionListener(e -> new Reports().setVisible(true));
    }
}
