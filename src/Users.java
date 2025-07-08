// src/Users.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;
import org.mindrot.jbcrypt.BCrypt;          // ← BCrypt import

public class Users extends JFrame {
    private JTable userTable;
    private DefaultTableModel tableModel;

    public Users() {
        setTitle("User Management");
        setSize(640, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ── Back button ─────────────────────────────────────────────
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton back = new JButton("← Back");
        back.addActionListener(e -> { dispose(); new Dashboard().setVisible(true); });
        top.add(back);
        add(top, BorderLayout.NORTH);

        // ── Table (NO password column) ──────────────────────────────
        String[] cols = {"ID", "Name", "Email"};
        tableModel = new DefaultTableModel(cols, 0);
        userTable  = new JTable(tableModel);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // ── Buttons panel ───────────────────────────────────────────
        JPanel buttons = new JPanel();
        JButton add    = new JButton("Add");
        JButton edit   = new JButton("Edit");
        JButton delete = new JButton("Delete");
        buttons.add(add); buttons.add(edit); buttons.add(delete);
        add(buttons, BorderLayout.SOUTH);

        // Load table
        loadUsers("");

        // Search (optional) ------------- you can comment this out
        JTextField search = new JTextField(15);
        top.add(new JLabel("Search:")); top.add(search);
        search.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadUsers(search.getText().trim());
            }
        });

        // ── Button actions ──────────────────────────────────────────
        add.addActionListener(e -> showUserForm(null));
        edit.addActionListener(e -> {
            int r = userTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this,"Select a user"); return; }
            int id    = (int)    tableModel.getValueAt(r,0);
            String nm = (String) tableModel.getValueAt(r,1);
            String em = (String) tableModel.getValueAt(r,2);
            showUserForm(new User(id,nm,em));
        });

        delete.addActionListener(e -> {
            int r = userTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this,"Select a user"); return; }
            int id = (int) tableModel.getValueAt(r,0);
            if (JOptionPane.showConfirmDialog(this,"Delete user?","Confirm",
                    JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                deleteUser(id); loadUsers("");
            }
        });
    }

    // ───────────────────────────────────────────────────────────────
    private void loadUsers(String keyword){
        tableModel.setRowCount(0);
        String sql="SELECT id,name,email FROM users WHERE name LIKE ? OR email LIKE ?";
        try(Connection c=DBConnection.getConnection();
            PreparedStatement s=c.prepareStatement(sql)){
            String k="%"+keyword+"%";
            s.setString(1,k); s.setString(2,k);
            ResultSet rs=s.executeQuery();
            while(rs.next()){
                Vector<Object> row=new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("email"));
                tableModel.addRow(row);
            }
        }catch(Exception ex){ex.printStackTrace();}
    }

    // ───────────────────────────────────────────────────────────────
    private void showUserForm(User u){
        JDialog d=new JDialog(this,u==null?"Add User":"Edit User",true);
        d.setSize(340,220); d.setLocationRelativeTo(this);
        d.setLayout(new GridBagLayout());
        GridBagConstraints g=new GridBagConstraints();
        g.insets=new Insets(8,8,8,8); g.fill=GridBagConstraints.HORIZONTAL;

        JTextField nameF =new JTextField(18);
        JTextField emailF=new JTextField(18);
        JPasswordField passF=new JPasswordField(18);

        if(u!=null){ nameF.setText(u.name); emailF.setText(u.email); }

        g.gridx=0; g.gridy=0; d.add(new JLabel("Name:"),g);
        g.gridx=1; d.add(nameF,g);
        g.gridx=0; g.gridy=1; d.add(new JLabel("Email:"),g);
        g.gridx=1; d.add(emailF,g);
        g.gridx=0; g.gridy=2; d.add(new JLabel(u==null?"Password:":"New Password:"),g);
        g.gridx=1; d.add(passF,g);

        JButton save=new JButton("Save");
        g.gridx=1; g.gridy=3; d.add(save,g);

        save.addActionListener(e->{
            String nm=nameF.getText().trim();
            String em=emailF.getText().trim();
            String pw=new String(passF.getPassword()).trim();
            if(nm.isEmpty()||em.isEmpty()||(u==null&&pw.isEmpty())){
                JOptionPane.showMessageDialog(d,"All fields required.");return;
            }
            if(u==null) addUser(nm,em,pw);           // new user
            else        updateUser(u.id,nm,em,pw);   // existing
            d.dispose(); loadUsers("");
        });
        d.setVisible(true);
    }

    // ───────────────────────────────────────────────────────────────
    private void addUser(String n,String e,String p){
        String hash=BCrypt.hashpw(p,BCrypt.gensalt());     // ← hash
        try(Connection c=DBConnection.getConnection()){
            PreparedStatement s=c.prepareStatement("INSERT INTO users(name,email,password) VALUES(?,?,?)");
            s.setString(1,n); s.setString(2,e); s.setString(3,hash);
            s.executeUpdate();
        }catch(SQLIntegrityConstraintViolationException dup){
            JOptionPane.showMessageDialog(this,"Email already exists.");
        }catch(Exception ex){ex.printStackTrace();}
    }

    private void updateUser(int id,String n,String e,String newPw){
        try(Connection c=DBConnection.getConnection()){
            String sql;
            PreparedStatement s;
            if(newPw.isEmpty()){                       // keep old hash
                sql="UPDATE users SET name=?,email=? WHERE id=?";
                s=c.prepareStatement(sql);
                s.setString(1,n); s.setString(2,e); s.setInt(3,id);
            }else{                                     // hash new password
                sql="UPDATE users SET name=?,email=?,password=? WHERE id=?";
                s=c.prepareStatement(sql);
                s.setString(1,n); s.setString(2,e);
                s.setString(3,BCrypt.hashpw(newPw,BCrypt.gensalt()));
                s.setInt(4,id);
            }
            s.executeUpdate();
        }catch(SQLIntegrityConstraintViolationException dup){
            JOptionPane.showMessageDialog(this,"Email already exists.");
        }catch(Exception ex){ex.printStackTrace();}
    }

    private void deleteUser(int id){
        try(Connection c=DBConnection.getConnection()){
            PreparedStatement s=c.prepareStatement("DELETE FROM users WHERE id=?");
            s.setInt(1,id); s.executeUpdate();
        }catch(Exception ex){ex.printStackTrace();}
    }

    // Simple User holder
    private static class User{
        int id; String name,email;
        User(int i,String n,String e){id=i;name=n;email=e;}
    }
}
