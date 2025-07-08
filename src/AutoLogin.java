import java.io.*;

public class AutoLogin {
    private static final String FILE_NAME = "autologin.dat";

    public static void saveEmail(String email) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(email);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSavedEmail() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String email = reader.readLine();
            return (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clearSavedEmail() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }
}
