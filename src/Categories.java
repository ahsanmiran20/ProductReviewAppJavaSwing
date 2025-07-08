import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class Categories extends JFrame {
    private JTable categoryTable;
    private DefaultTableModel tableModel;

    public Categories() {
        setTitle("Categories Management");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table columns
        String[] columns = {"ID", "Category Name"};
        tableModel = new DefaultTableModel(columns, 0);
        categoryTable = new JTable(tableModel);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoryTable);

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

        loadCategories();

        addBtn.addActionListener(e -> showCategoryForm(null));
        editBtn.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a category to edit.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = (String) tableModel.getValueAt(selectedRow, 1);
            showCategoryForm(new Category(id, name));
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Select a category to delete.");
                return;
            }
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected category?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteCategory(id);
            }
        });
    }

    private void loadCategories() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name FROM categories";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                tableModel.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load categories.");
        }
    }

    private void showCategoryForm(Category category) {
        JDialog dialog = new JDialog(this, category == null ? "Add Category" : "Edit Category", true);
        dialog.setSize(350, 150);
        dialog.setLayout(new GridBagLayout());
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel nameLabel = new JLabel("Category Name:");
        JTextField nameField = new JTextField(20);

        if (category != null) {
            nameField.setText(category.name);
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        dialog.add(nameLabel, gbc);

        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        JButton saveBtn = new JButton("Save");
        gbc.gridx = 1;
        gbc.gridy = 1;
        dialog.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Category name cannot be empty.");
                return;
            }

            if (category == null) {
                addCategory(name);
            } else {
                updateCategory(category.id, name);
            }
            dialog.dispose();
            loadCategories();
        });

        dialog.setVisible(true);
    }

    private void addCategory(String name) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO categories (name) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category added successfully.");
        } catch (SQLIntegrityConstraintViolationException dupEx) {
            JOptionPane.showMessageDialog(this, "Category already exists.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add category.");
        }
    }

    private void updateCategory(int id, String name) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE categories SET name = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category updated successfully.");
        } catch (SQLIntegrityConstraintViolationException dupEx) {
            JOptionPane.showMessageDialog(this, "Category already exists.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update category.");
        }
    }

    private void deleteCategory(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM categories WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Category deleted successfully.");
            loadCategories();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete category.");
        }
    }

    private static class Category {
        int id;
        String name;

        Category(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
