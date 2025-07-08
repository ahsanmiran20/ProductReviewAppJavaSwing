// Paste this in your Products.java under src/
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class Products extends JFrame {
    private JTable productTable;
    private DefaultTableModel tableModel;

    public Products() {
        setTitle("Products Management");
        setSize(750, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Description", "Price", "Category"};
        tableModel = new DefaultTableModel(columns, 0);
        productTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        add(btnPanel, BorderLayout.SOUTH);

        loadProducts("");

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String keyword = searchField.getText().trim();
                loadProducts(keyword);
            }
        });

        addBtn.addActionListener(e -> showProductForm(null));
        editBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a product first.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String name = (String) tableModel.getValueAt(row, 1);
            String desc = (String) tableModel.getValueAt(row, 2);
            double price = (double) tableModel.getValueAt(row, 3);
            String category = (String) tableModel.getValueAt(row, 4);
            showProductForm(new Product(id, name, desc, price, category));
        });

        deleteBtn.addActionListener(e -> {
            int row = productTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a product to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Confirm delete?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteProduct(id);
            }
        });
    }

    private void loadProducts(String keyword) {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT p.id, p.name, p.description, p.price, c.name AS category_name " +
                    "FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
                    "WHERE p.name LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("description"));
                row.add(rs.getDouble("price"));
                row.add(rs.getString("category_name"));
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products.");
        }
    }

    private void showProductForm(Product product) {
        JDialog dialog = new JDialog(this, product == null ? "Add Product" : "Edit Product", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(20);
        JLabel descLabel = new JLabel("Description:");
        JTextArea descArea = new JTextArea(3, 20);
        JScrollPane descScroll = new JScrollPane(descArea);
        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField(20);
        JLabel catLabel = new JLabel("Category:");
        JComboBox<String> catBox = new JComboBox<>();
        for (String cat : fetchCategories()) catBox.addItem(cat);

        if (product != null) {
            nameField.setText(product.name);
            descArea.setText(product.description);
            priceField.setText(String.valueOf(product.price));
            catBox.setSelectedItem(product.category);
        }

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(nameLabel, gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(descLabel, gbc);
        gbc.gridx = 1; dialog.add(descScroll, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(priceLabel, gbc);
        gbc.gridx = 1; dialog.add(priceField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(catLabel, gbc);
        gbc.gridx = 1; dialog.add(catBox, gbc);
        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1; gbc.gridy = 4;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descArea.getText().trim();
            String priceStr = priceField.getText().trim();
            String cat = (String) catBox.getSelectedItem();
            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill name and price.");
                return;
            }
            try {
                double price = Double.parseDouble(priceStr);
                if (product == null)
                    addProduct(name, desc, price, cat);
                else
                    updateProduct(product.id, name, desc, price, cat);
                dialog.dispose();
                loadProducts("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid price.");
            }
        });

        dialog.setVisible(true);
    }

    private java.util.List<String> fetchCategories() {
        java.util.List<String> categories = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM categories");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) categories.add(rs.getString("name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    private void addProduct(String name, String desc, double price, String categoryName) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement catStmt = conn.prepareStatement("SELECT id FROM categories WHERE name = ?");
            catStmt.setString(1, categoryName);
            ResultSet rs = catStmt.executeQuery();
            Integer catId = null;
            if (rs.next()) catId = rs.getInt("id");

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO products (name, description, price, category_id) VALUES (?, ?, ?, ?)");
            stmt.setString(1, name);
            stmt.setString(2, desc);
            stmt.setDouble(3, price);
            if (catId != null) stmt.setInt(4, catId); else stmt.setNull(4, Types.INTEGER);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateProduct(int id, String name, String desc, double price, String categoryName) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement catStmt = conn.prepareStatement("SELECT id FROM categories WHERE name = ?");
            catStmt.setString(1, categoryName);
            ResultSet rs = catStmt.executeQuery();
            Integer catId = null;
            if (rs.next()) catId = rs.getInt("id");

            PreparedStatement stmt = conn.prepareStatement("UPDATE products SET name = ?, description = ?, price = ?, category_id = ? WHERE id = ?");
            stmt.setString(1, name);
            stmt.setString(2, desc);
            stmt.setDouble(3, price);
            if (catId != null) stmt.setInt(4, catId); else stmt.setNull(4, Types.INTEGER);
            stmt.setInt(5, id);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteProduct(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
            loadProducts("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Product {
        int id;
        String name, description, category;
        double price;

        Product(int id, String name, String description, double price, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
        }
    }
}
