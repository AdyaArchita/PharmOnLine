package edu.pharmacy.desktop;

import edu.pharmacy.desktop.chat.ChatClient;
import edu.pharmacy.model.Person;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/** Main staff workspace, organized into the requested four dashboard tabs. */
public class DashboardPanel extends JPanel {
    public DashboardPanel(Person person, Runnable onLogout) {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING));
        header.add(new JLabel("Signed in: " + person.getFullName()
                + " (" + person.getRole() + ")"));

        JButton quickPriceButton = new JButton("Quick Price");
        quickPriceButton.addActionListener(event -> new QuickPriceChecker());

        JButton supportChatButton = new JButton("Support Chat");
        supportChatButton.addActionListener(event ->
                SwingUtilities.invokeLater(() -> new ChatClient("localhost",
                        edu.pharmacy.desktop.chat.ChatServer.DEFAULT_PORT,
                        person.getFullName()).setVisible(true)));

        JButton logoutButton = new JButton("Sign out");
        logoutButton.addActionListener(event -> onLogout.run());

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        rightSide.add(quickPriceButton);
        rightSide.add(supportChatButton);
        rightSide.add(logoutButton);
        header.add(rightSide);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Medicines", new MedicinePanel());
        tabs.addTab("Suppliers", new SupplierPanel());
        tabs.addTab("Orders", new OrderPanel());
        tabs.addTab("Prescriptions", new PrescriptionPanel());
        tabs.addTab("Reports", new ReportPanel());
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(event -> {
            java.awt.Component selected = tabs.getSelectedComponent();
            if (selected instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        });
    }

    interface Refreshable {
        void refresh();
    }
}
