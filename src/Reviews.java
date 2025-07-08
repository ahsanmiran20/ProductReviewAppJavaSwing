import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class Reviews extends JFrame {
    private JTable reviewTable;
    private DefaultTableModel tableModel;

    public Reviews() {
        setTitle("Reviews Management");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = {"ID", "User", "Product", "Rating", "Review Text", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        reviewTable = new JTable(tableModel);
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(reviewTable);

        // Buttons panel
        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadReviews();

        addBtn.addActionListener(e -> showReviewForm(null));
        editBtn.addActionListener(e -> {
            int selectedRow = reviewTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a review to edit.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String user = (String) tableModel.getValueAt(selectedRow, 1);
            String product = (String) tableModel.getValueAt(selectedRow, 2);
            int rating = (int) tableModel.getValueAt(selectedRow, 3);
            String reviewText = (String) tableModel.getValueAt(selectedRow, 4);

            Review r = new Review(id, user, product, rating, reviewText);
            showReviewForm(r);
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = reviewTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a review to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected review?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteReview(id);
            }
        });
    }

    private void loadReviews() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.id, u.name AS user_name, p.name AS product_name, r.rating, r.review_text, r.review_date " +
                    "FROM reviews r " +
                    "JOIN users u ON r.user_id = u.id " +
                    "JOIN products p ON r.product_id = p.id " +
                    "ORDER BY r.review_date DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("user_name"));
                row.add(rs.getString("product_name"));
                row.add(rs.getInt("rating"));
                row.add(rs.getString("review_text"));
                row.add(rs.getTimestamp("review_date"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load reviews.");
        }
    }

    private void showReviewForm(Review review) {
        JDialog dialog = new JDialog(this, review == null ? "Add Review" : "Edit Review", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("User:");
        JLabel productLabel = new JLabel("Product:");
        JLabel ratingLabel = new JLabel("Rating (1-5):");
        JLabel reviewLabel = new JLabel("Review:");

        JComboBox<String> userBox = new JComboBox<>();
        for (String u : fetchUsers()) userBox.addItem(u);

        JComboBox<String> productBox = new JComboBox<>();
        for (String p : fetchProducts()) productBox.addItem(p);

        JTextField ratingField = new JTextField(20);
        JTextArea reviewArea = new JTextArea(4, 20);
        JScrollPane reviewScroll = new JScrollPane(reviewArea);

        if (review != null) {
            userBox.setSelectedItem(review.user);
            productBox.setSelectedItem(review.product);
            ratingField.setText(String.valueOf(review.rating));
            reviewArea.setText(review.reviewText);
        }

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(userLabel, gbc);
        gbc.gridx = 1; dialog.add(userBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(productLabel, gbc);
        gbc.gridx = 1; dialog.add(productBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(ratingLabel, gbc);
        gbc.gridx = 1; dialog.add(ratingField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(reviewLabel, gbc);
        gbc.gridx = 1; dialog.add(reviewScroll, gbc);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1; gbc.gridy = 4; dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String user = (String) userBox.getSelectedItem();
            String product = (String) productBox.getSelectedItem();
            String ratingStr = ratingField.getText().trim();
            String reviewText = reviewArea.getText().trim();

            if (user == null || product == null || ratingStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "User, product, and rating are required.");
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingStr);
                if (rating < 1 || rating > 5) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Rating must be an integer between 1 and 5.");
                return;
            }

            if (review == null) {
                addReview(user, product, rating, reviewText);
            } else {
                updateReview(review.id, user, product, rating, reviewText);
            }
            dialog.dispose();
            loadReviews();
        });

        dialog.setVisible(true);
    }

    private java.util.List<String> fetchUsers() {
        java.util.List<String> users = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) users.add(rs.getString("name"));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load users.");
        }
        return users;
    }

    private java.util.List<String> fetchProducts() {
        java.util.List<String> products = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM products");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) products.add(rs.getString("name"));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load products.");
        }
        return products;
    }

    private void addReview(String userName, String productName, int rating, String reviewText) {
        try (Connection conn = DBConnection.getConnection()) {
            int userId = getUserIdByName(conn, userName);
            int productId = getProductIdByName(conn, productName);

            if (userId == -1 || productId == -1) {
                JOptionPane.showMessageDialog(this, "Invalid user or product.");
                return;
            }

            String sql = "INSERT INTO reviews (user_id, product_id, rating, review_text, review_date) VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, rating);
            stmt.setString(4, reviewText);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Review added successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add review.");
        }
    }

    private void updateReview(int id, String userName, String productName, int rating, String reviewText) {
        try (Connection conn = DBConnection.getConnection()) {
            int userId = getUserIdByName(conn, userName);
            int productId = getProductIdByName(conn, productName);

            if (userId == -1 || productId == -1) {
                JOptionPane.showMessageDialog(this, "Invalid user or product.");
                return;
            }

            String sql = "UPDATE reviews SET user_id = ?, product_id = ?, rating = ?, review_text = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, rating);
            stmt.setString(4, reviewText);
            stmt.setInt(5, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Review updated successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update review.");
        }
    }

    private void deleteReview(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM reviews WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Review deleted successfully.");
            loadReviews();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete review.");
        }
    }

    private int getUserIdByName(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }

    private int getProductIdByName(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM products WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }

    private static class Review {
        int id;
        String user, product, reviewText;
        int rating;

        Review(int id, String user, String product, int rating, String reviewText) {
            this.id = id;
            this.user = user;
            this.product = product;
            this.rating = rating;
            this.reviewText = reviewText;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Reviews().setVisible(true));
    }
}
