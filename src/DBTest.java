import java.sql.Connection;

public class DBTest {
    public static void main(String[] args) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                System.out.println("✅ Database connection successful!");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to connect: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
