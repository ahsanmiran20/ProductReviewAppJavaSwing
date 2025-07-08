import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

public class Login extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public Login() {
        setTitle("Login - Product Review App");
        setSize(350, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel emailLabel = new JLabel("Email:");
        JLabel passLabel = new JLabel("Password:");

        emailField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(this::handleLogin);

        JButton signupBtn = new JButton("Sign Up");
        signupBtn.addActionListener(evt -> new Register().setVisible(true)); // Assume Register exists

        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.gridx = 0; gbc.gridy = 0; add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(15, 10, 5, 10);
        add(loginBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        add(signupBtn, gbc);

        // Auto login check
        String savedEmail = AutoLogin.getSavedEmail();
        if (savedEmail != null) {
            emailField.setText(savedEmail);
            // Optionally auto-login automatically here, or require password
            // For safety, just fill email so user can enter password faster
        }
    }

    private void handleLogin(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    JOptionPane.showMessageDialog(this,
                            "Login successful! Welcome, " + rs.getString("name"),
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    AutoLogin.saveEmail(email);  // Save email for auto-login next time

                    new WelcomePage(email).setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid email or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid email or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error connecting to database.",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
