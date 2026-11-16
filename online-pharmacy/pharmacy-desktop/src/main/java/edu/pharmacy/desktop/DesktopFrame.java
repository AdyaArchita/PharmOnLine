package edu.pharmacy.desktop;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.model.Person;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/** Holds the login and dashboard cards in one application window. */
public class DesktopFrame extends JFrame {
    private static final String LOGIN_CARD = "login";
    private static final String DASHBOARD_CARD = "dashboard";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final LoginPanel loginPanel;
    private Thread stockMonitorThread;

    public DesktopFrame() {
        super("Online Pharmacy - Staff");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        loginPanel = new LoginPanel(this::showDashboard);
        cards.add(loginPanel, LOGIN_CARD);
        setContentPane(cards);
        cardLayout.show(cards, LOGIN_CARD);
    }

    private void showDashboard(Person person) {
        startStockMonitor();
        cards.add(new DashboardPanel(person, this::showLogin), DASHBOARD_CARD);
        cardLayout.show(cards, DASHBOARD_CARD);
        revalidate();
        repaint();
    }

    private void showLogin() {
        stopStockMonitor();
        cards.remove(cards.getComponentCount() - 1);
        loginPanel.clearPassword();
        cardLayout.show(cards, LOGIN_CARD);
        revalidate();
        repaint();
    }

    private void startStockMonitor() {
        StockMonitor monitor = new StockMonitor(new MedicineDAO(), 10, 30, 60);
        stockMonitorThread = new Thread(monitor, "pharmacy-stock-monitor");
        stockMonitorThread.setDaemon(true);
        stockMonitorThread.start();
    }

    private void stopStockMonitor() {
        if (stockMonitorThread != null) {
            stockMonitorThread.interrupt();
            stockMonitorThread = null;
        }
    }
}
