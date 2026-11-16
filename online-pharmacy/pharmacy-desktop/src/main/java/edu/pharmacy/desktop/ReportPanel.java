package edu.pharmacy.desktop;

import edu.pharmacy.dao.OrderDAO;
import edu.pharmacy.model.CustomerOrder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** Simple daily summary; background file generation is added in the threads lab stage. */
public class ReportPanel extends JPanel implements DashboardPanel.Refreshable {
    private final JLabel summary = new JLabel("Select Refresh to calculate today's sales.");
    private final OrderDAO orderDAO = new OrderDAO();

    public ReportPanel() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING));
        top.add(summary);
        JButton refreshButton = new JButton("Refresh daily sales");
        refreshButton.addActionListener(event -> refresh());
        top.add(refreshButton);
        JButton generateFileButton = new JButton("Generate text report");
        generateFileButton.addActionListener(event -> new ReportGenerator().start());
        top.add(generateFileButton);
        add(top, BorderLayout.NORTH);
    }

    @Override
    public void refresh() {
        summary.setText("Calculating today's sales...");
        new SwingWorker<List<CustomerOrder>, Void>() {
            @Override
            protected List<CustomerOrder> doInBackground() throws Exception {
                return orderDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    LocalDate today = LocalDate.now();
                    List<CustomerOrder> completedToday = get().stream()
                            .filter(order -> order.getCreatedAt().toLocalDate().equals(today))
                            .filter(order -> order.getStatus().equals("COMPLETED"))
                            .toList();
                    BigDecimal total = completedToday.stream()
                            .map(CustomerOrder::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    summary.setText("Completed orders today: " + completedToday.size()
                            + "    Sales total: INR " + total.toPlainString());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError(exception);
                } catch (ExecutionException exception) {
                    showError(exception.getCause());
                }
            }
        }.execute();
    }

    private void showError(Throwable error) {
        System.getLogger(ReportPanel.class.getName()).log(System.Logger.Level.ERROR,
                "Daily sales report failed.", error);
        summary.setText("Daily sales report could not be generated.");
        JOptionPane.showMessageDialog(this, "Report data could not be loaded.",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
