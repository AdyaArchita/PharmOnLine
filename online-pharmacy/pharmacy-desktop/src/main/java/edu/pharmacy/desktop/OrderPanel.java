package edu.pharmacy.desktop;

import edu.pharmacy.dao.OrderDAO;
import edu.pharmacy.model.CustomerOrder;
import edu.pharmacy.desktop.files.Invoice;
import edu.pharmacy.desktop.files.InvoiceFileStore;
import edu.pharmacy.desktop.files.InvoiceLine;
import edu.pharmacy.desktop.files.ReportFiles;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;

/** Order queue and status update controls for staff. */
public class OrderPanel extends JPanel implements DashboardPanel.Refreshable {
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderTableModel tableModel = new OrderTableModel();
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> statusBox = new JComboBox<>(
            new String[]{"PLACED", "PROCESSING", "READY", "COMPLETED", "CANCELLED"});
    private final JButton updateButton = new JButton("Update status");
    private final JButton invoiceButton = new JButton("Save/load invoice");

    public OrderPanel() {
        setLayout(new BorderLayout());
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);
        updateButton.setEnabled(false);
        invoiceButton.setEnabled(false);
        updateButton.addActionListener(event -> updateStatus());
        invoiceButton.addActionListener(event -> saveInvoice());
        JButton refreshButton = new JButton("Refresh orders");
        refreshButton.addActionListener(event -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING));
        actions.add(statusBox);
        actions.add(updateButton);
        actions.add(invoiceButton);
        actions.add(refreshButton);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        new SwingWorker<List<CustomerOrder>, Void>() {
            @Override
            protected List<CustomerOrder> doInBackground() throws SQLException {
                return orderDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRows(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError(exception);
                } catch (ExecutionException exception) {
                    showError(exception.getCause());
                }
            }
        }.execute();
    }

    private void selectionChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            int row = table.getSelectedRow();
            updateButton.setEnabled(row >= 0);
            invoiceButton.setEnabled(row >= 0);
            if (row >= 0) {
                statusBox.setSelectedItem(tableModel.getOrderAt(
                        table.convertRowIndexToModel(row)).getStatus());
            }
        }
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        CustomerOrder order = tableModel.getOrderAt(table.convertRowIndexToModel(row));
        String status = (String) statusBox.getSelectedItem();
        if ("CANCELLED".equals(status)
                && JOptionPane.showConfirmDialog(this,
                        "Cancel this order and return its items to stock?",
                        "Confirm cancellation", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (status != null && !orderDAO.updateStatus(order.getId(), status)) {
                JOptionPane.showMessageDialog(this, "The order was not found.",
                        "Update order", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Invalid status change", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void saveInvoice() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        CustomerOrder order = tableModel.getOrderAt(table.convertRowIndexToModel(row));
        try {
            List<InvoiceLine> lines = orderDAO.findItemsByOrderId(order.getId()).stream()
                    .map(line -> new InvoiceLine(line.getMedicineId(), line.getMedicineName(),
                            line.getQuantity(), line.getUnitPrice()))
                    .toList();
            Invoice invoice = new Invoice(order.getId(), order.getCustomerId(),
                    order.getCreatedAt(), lines, order.getTotalAmount());
            Path file = ReportFiles.invoiceFile(order.getId());
            InvoiceFileStore.save(invoice, file);
            Invoice loaded = InvoiceFileStore.load(file);
            JOptionPane.showMessageDialog(this,
                    "Invoice saved and loaded successfully.\nFile: " + file
                            + "\nLine items: " + loaded.getLines().size()
                            + "\nTotal: INR " + loaded.getTotalAmount(),
                    "Invoice archive", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException | IOException | ClassNotFoundException exception) {
            showError(exception);
        }
    }

    private void showError(Throwable error) {
        System.getLogger(OrderPanel.class.getName()).log(System.Logger.Level.ERROR,
                "Order operation failed.", error);
        JOptionPane.showMessageDialog(this, "Order data could not be loaded or updated.",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
