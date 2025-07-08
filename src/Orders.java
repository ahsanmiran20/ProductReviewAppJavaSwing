import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.List; // This is from java.util

public class Orders extends JFrame {
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public Orders() {
        setTitle("Orders Management");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columns = {"Order ID", "User", "Product", "Quantity", "Total Price", "Order Date"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton backBtn = new JButton("← Back");

        btnPanel.add(backBtn);
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        loadOrders();

        backBtn.addActionListener(e -> this.dispose());

        addBtn.addActionListener(e -> showOrderForm(null));

        editBtn.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an order to edit.");
                return;
            }

            int orderId = (int) tableModel.getValueAt(row, 0);
            String user = (String) tableModel.getValueAt(row, 1);
            String product = (String) tableModel.getValueAt(row, 2);
            int quantity = (int) tableModel.getValueAt(row, 3);

            OrderData od = new OrderData(orderId, user, product, quantity);
            showOrderForm(od);
        });

        deleteBtn.addActionListener(e -> {
            int row = orderTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an order to delete.");
                return;
            }

            int orderId = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete this order?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteOrder(orderId);
            }
        });
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT o.id, u.name AS user, p.name AS product, o.quantity, o.total_price, o.order_date " +
                    "FROM orders o JOIN users u ON o.user_id = u.id " +
                    "JOIN products p ON o.product_id = p.id";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("user"),
                        rs.getString("product"),
                        rs.getInt("quantity"),
                        rs.getDouble("total_price"),
                        rs.getDate("order_date")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load orders.");
        }
    }

    private void showOrderForm(OrderData order) {
        JDialog dialog = new JDialog(this, order == null ? "Add Order" : "Edit Order", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));

        JComboBox<String> userBox = new JComboBox<>(fetchUsers());
        JComboBox<String> productBox = new JComboBox<>(fetchProducts());
        JTextField qtyField = new JTextField();

        if (order != null) {
            userBox.setSelectedItem(order.user);
            productBox.setSelectedItem(order.product);
            qtyField.setText(String.valueOf(order.quantity));
        }

        dialog.add(new JLabel("User:"));
        dialog.add(userBox);
        dialog.add(new JLabel("Product:"));
        dialog.add(productBox);
        dialog.add(new JLabel("Quantity:"));
        dialog.add(qtyField);
        dialog.add(new JLabel());
        JButton saveBtn = new JButton("Save");
        dialog.add(saveBtn);

        saveBtn.addActionListener(e -> {
            String user = (String) userBox.getSelectedItem();
            String product = (String) productBox.getSelectedItem();
            int qty;

            try {
                qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Enter valid quantity.");
                return;
            }

            if (order == null) {
                insertOrder(user, product, qty);
            } else {
                updateOrder(order.id, user, product, qty);
            }

            dialog.dispose();
            loadOrders();
        });

        dialog.setVisible(true);
    }

    private void insertOrder(String user, String product, int qty) {
        try (Connection conn = DBConnection.getConnection()) {
            int userId = getUserId(conn, user);
            int productId = getProductId(conn, product);
            double unitPrice = getProductPrice(conn, product);
            double total = unitPrice * qty;
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            String sql = "INSERT INTO orders (user_id, product_id, quantity, total_price, order_date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, qty);
            stmt.setDouble(4, total);
            stmt.setDate(5, today);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order added.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateOrder(int id, String user, String product, int qty) {
        try (Connection conn = DBConnection.getConnection()) {
            int userId = getUserId(conn, user);
            int productId = getProductId(conn, product);
            double unitPrice = getProductPrice(conn, product);
            double total = unitPrice * qty;

            String sql = "UPDATE orders SET user_id = ?, product_id = ?, quantity = ?, total_price = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, qty);
            stmt.setDouble(4, total);
            stmt.setInt(5, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Order updated.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteOrder(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "DELETE FROM orders WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Order deleted.");
            loadOrders();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] fetchUsers() {
        List<String> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT name FROM users").executeQuery();
            while (rs.next()) users.add(rs.getString("name"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return users.toArray(new String[0]);
    }

    private String[] fetchProducts() {
        List<String> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT name FROM products").executeQuery();
            while (rs.next()) products.add(rs.getString("name"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return products.toArray(new String[0]);
    }

    private int getUserId(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    private int getProductId(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM products WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    private double getProductPrice(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT price FROM products WHERE name = ?");
        stmt.setString(1, name);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getDouble(1) : 0;
    }

    static class OrderData {
        int id, quantity;
        String user, product;
        OrderData(int id, String user, String product, int quantity) {
            this.id = id;
            this.user = user;
            this.product = product;
            this.quantity = quantity;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Orders().setVisible(true));
    }
}
