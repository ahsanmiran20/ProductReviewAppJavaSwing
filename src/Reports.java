import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Reports extends JFrame {
    private DefaultTableModel tableModel;
    private JTable reportTable;
    private Connection con;

    public Reports() {
        setTitle("Reports");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"Report Type", "Count"};
        tableModel = new DefaultTableModel(columns, 0);
        reportTable = new JTable(tableModel);
        add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Refresh button
        JButton btnRefresh = new JButton("Refresh Reports");
        btnRefresh.addActionListener(e -> loadReportData());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnRefresh);
        add(btnPanel, BorderLayout.SOUTH);

        // Connect and load data
        connectDB();
        loadReportData();
    }

    private void connectDB() {
        String url = "jdbc:mysql://localhost:3306/product_review_db";
        String user = "ahsanmiran";
        String pass = "MiraN4582#";
        try {
            con = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadReportData() {
        if (con == null) return;

        // Clear old data
        tableModel.setRowCount(0);

        try {
            Statement stmt = con.createStatement();

            // Total users
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) {
                tableModel.addRow(new Object[]{"Total Users", rs.getInt(1)});
            }
            rs.close();

            // Total products
            rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next()) {
                tableModel.addRow(new Object[]{"Total Products", rs.getInt(1)});
            }
            rs.close();

            // Total orders
            rs = stmt.executeQuery("SELECT COUNT(*) FROM orders");
            if (rs.next()) {
                tableModel.addRow(new Object[]{"Total Orders", rs.getInt(1)});
            }
            rs.close();

            // Total reviews
            rs = stmt.executeQuery("SELECT COUNT(*) FROM reviews");
            if (rs.next()) {
                tableModel.addRow(new Object[]{"Total Reviews", rs.getInt(1)});
            }
            rs.close();

            stmt.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load report data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Reports().setVisible(true));
    }
}
