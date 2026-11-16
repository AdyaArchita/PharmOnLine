package edu.pharmacy.desktop;

import edu.pharmacy.dao.SupplierDAO;
import edu.pharmacy.model.Supplier;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;

/** Supplier CRUD tab in the staff dashboard. */
public class SupplierPanel extends JPanel implements DashboardPanel.Refreshable {
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final SupplierTableModel tableModel = new SupplierTableModel();
    private final JTable table = new JTable(tableModel);
    private final JButton editButton = new JButton("Edit selected");
    private final JButton deleteButton = new JButton("Delete selected");

    public SupplierPanel() {
        setLayout(new BorderLayout());
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton addButton = new JButton("Add supplier");
        JButton refreshButton = new JButton("Refresh");
        addButton.addActionListener(event -> editSupplier(null));
        refreshButton.addActionListener(event -> refresh());
        editButton.addActionListener(event -> editSupplier(selectedSupplier()));
        deleteButton.addActionListener(event -> deleteSupplier());
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        new SwingWorker<List<Supplier>, Void>() {
            @Override
            protected List<Supplier> doInBackground() throws SQLException {
                return supplierDAO.findAll();
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
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        }
    }

    private Supplier selectedSupplier() {
        int row = table.getSelectedRow();
        return row < 0 ? null : tableModel.getSupplierAt(table.convertRowIndexToModel(row));
    }

    private void editSupplier(Supplier current) {
        SupplierDialog dialog = new SupplierDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), current);
        dialog.setVisible(true);
        Supplier edited = dialog.getResult();
        if (edited == null) {
            return;
        }
        try {
            if (current == null) {
                supplierDAO.create(edited);
            } else if (!supplierDAO.update(edited)) {
                JOptionPane.showMessageDialog(this, "The supplier was not found.",
                        "Save supplier", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void deleteSupplier() {
        Supplier supplier = selectedSupplier();
        if (supplier == null || JOptionPane.showConfirmDialog(this,
                "Delete " + supplier.getName() + "?", "Confirm deletion",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!supplierDAO.delete(supplier.getId())) {
                JOptionPane.showMessageDialog(this, "The supplier was not found.",
                        "Delete supplier", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void showError(Throwable error) {
        System.getLogger(SupplierPanel.class.getName()).log(System.Logger.Level.ERROR,
                "Supplier operation failed.", error);
        JOptionPane.showMessageDialog(this, "Supplier data could not be loaded or saved.",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }
}
