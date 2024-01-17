import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AuthenticationDialog extends JDialog {
    private JTextField userField;
    private JPasswordField passwordField;
    private boolean authenticated = false;

    private String jdbcUrl;
    private String dbUser;
    private String dbPassword;

    public AuthenticationDialog(Frame owner) {
        super(owner, "Аутентификация пользователя в базе данных", true);
        setSize(300, 150);
        setLocationRelativeTo(owner);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        userField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Логин:"));
        panel.add(userField);
        panel.add(new JLabel("Пароль:"));
        panel.add(passwordField);

        JButton loginButton = new JButton("Войти");
        loginButton.addActionListener(e -> {
            jdbcUrl = "jdbc:mysql://localhost:3306/collection";
            dbUser = userField.getText();
            dbPassword = new String(passwordField.getPassword());

            authenticated = authenticateDatabase();
            synchronized (this) {
                notify();
            }
            dispose();
        });

        panel.add(loginButton);

        add(panel);
    }

    private boolean authenticateDatabase() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}