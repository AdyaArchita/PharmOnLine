package edu.pharmacy.desktop;

import edu.pharmacy.dao.UserDAO;
import edu.pharmacy.model.Person;
import edu.pharmacy.model.Role;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/** Staff login form; customer accounts are intentionally rejected here. */
public class LoginPanel extends JPanel {
    private final JTextField emailField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);

    public LoginPanel(Consumer<Person> onLogin) {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.anchor = GridBagConstraints.LINE_END;

        add(new JLabel("Staff sign in"), constraints(0, 0, constraints));
        constraints.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Email:"), constraints(0, 1, constraints));
        constraints.anchor = GridBagConstraints.LINE_START;
        add(emailField, constraints(1, 1, constraints));
        constraints.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Password:"), constraints(0, 2, constraints));
        constraints.anchor = GridBagConstraints.LINE_START;
        add(passwordField, constraints(1, 2, constraints));

        JButton loginButton = new JButton("Sign in");
        loginButton.addActionListener(event -> authenticate(loginButton, onLogin));
        GridBagConstraints buttonConstraints = constraints(1, 3, constraints);
        buttonConstraints.anchor = GridBagConstraints.LINE_START;
        add(loginButton, buttonConstraints);
    }

    private static GridBagConstraints constraints(int x, int y, GridBagConstraints base) {
        base.gridx = x;
        base.gridy = y;
        return base;
    }

    private void authenticate(JButton loginButton, Consumer<Person> onLogin) {
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();
        if (email.isBlank() || password.length < 8 || password.length > 128) {
            Arrays.fill(password, '\0');
            JOptionPane.showMessageDialog(this, "Enter a valid email and password.",
                    "Sign in", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);
        new SwingWorker<Person, Void>() {
            @Override
            protected Person doInBackground() throws SQLException, GeneralSecurityException {
                return new UserDAO().authenticate(email, password)
                        .filter(person -> person.getRole() != Role.CUSTOMER)
                        .orElse(null);
            }

            @Override
            protected void done() {
                Arrays.fill(password, '\0');
                loginButton.setEnabled(true);
                try {
                    Person person = get();
                    if (person == null) {
                        JOptionPane.showMessageDialog(LoginPanel.this,
                                "Staff credentials were not accepted.",
                                "Sign in", JOptionPane.WARNING_MESSAGE);
                    } else {
                        onLogin.accept(person);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showDatabaseError(exception);
                } catch (java.util.concurrent.ExecutionException exception) {
                    showDatabaseError(exception.getCause());
                }
            }
        }.execute();
    }

    private void showDatabaseError(Throwable error) {
        System.getLogger(LoginPanel.class.getName()).log(System.Logger.Level.ERROR,
                "Staff login failed.", error);
        JOptionPane.showMessageDialog(this,
                "Could not connect to the pharmacy database. Check your DB settings.",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    public void clearPassword() {
        passwordField.setText("");
    }
}
