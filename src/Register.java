import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.mindrot.jbcrypt.BCrypt;

public class Register extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;

    public Register() {
        setTitle("Register - Product Review App");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);

        JLabel nameLabel = new JLabel("Full Name:");
        JLabel emailLabel = new JLabel("Email:");
        JLabel passLabel = new JLabel("Password:");

        nameField = new JTextField(18);
        emailField = new JTextField(18);
        passwordField = new JPasswordField(18);

        JButton registerBtn = new JButton("Register");
        registerBtn.addActionListener(this::handleRegister);

        gbc.gridx = 0; gbc.gridy = 0; add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(emailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; add(passwordField, gbc);

        gbc.insets = new Insets(15, 10, 10, 10);
        gbc.gridx = 1; gbc.gridy = 3;
        add(registerBtn, gbc);
    }

    private void handleRegister(ActionEvent e) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Check if user already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Email already registered. Please log in.",
                        "Already Registered", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Hash password
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

            // Insert new user with hashed password
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, hashed);

            int inserted = stmt.executeUpdate();
            if (inserted > 0) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You can now log in.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Registration failed. Try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error. Try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
