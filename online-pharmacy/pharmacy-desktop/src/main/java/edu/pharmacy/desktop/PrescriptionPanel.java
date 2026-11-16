package edu.pharmacy.desktop;

import edu.pharmacy.dao.PrescriptionDAO;
import edu.pharmacy.model.PrescriptionRecord;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

/** Lets pharmacists inspect and approve or reject uploaded prescription files. */
public class PrescriptionPanel extends JPanel implements DashboardPanel.Refreshable {
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final PrescriptionTableModel tableModel = new PrescriptionTableModel();
    private final JTable table = new JTable(tableModel);
    private final JButton openButton = new JButton("Open file");
    private final JButton approveButton = new JButton("Approve");
    private final JButton rejectButton = new JButton("Reject");

    public PrescriptionPanel() {
        setLayout(new BorderLayout());
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);
        openButton.setEnabled(false);
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        openButton.addActionListener(event -> openFile());
        approveButton.addActionListener(event -> review("APPROVED"));
        rejectButton.addActionListener(event -> review("REJECTED"));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING));
        actions.add(openButton);
        actions.add(approveButton);
        actions.add(rejectButton);
        actions.add(refreshButton);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        new SwingWorker<List<PrescriptionRecord>, Void>() {
            @Override
            protected List<PrescriptionRecord> doInBackground() throws SQLException {
                return prescriptionDAO.findPending();
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
            openButton.setEnabled(selected);
            approveButton.setEnabled(selected);
            rejectButton.setEnabled(selected);
        }
    }

    private PrescriptionRecord selectedRecord() {
        int row = table.getSelectedRow();
        return row < 0 ? null
                : tableModel.getRecordAt(table.convertRowIndexToModel(row));
    }

    private void openFile() {
        PrescriptionRecord record = selectedRecord();
        if (record == null) {
            return;
        }
        try {
            Path directory = uploadDirectory();
            Path file = directory.resolve(record.getFileName()).normalize();
            if (!file.getParent().equals(directory) || !Files.isRegularFile(file)) {
                throw new IOException("The uploaded file is not available in the configured folder.");
            }
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                throw new IOException("No desktop file viewer is available.");
            }
            Desktop.getDesktop().open(file.toFile());
        } catch (IOException | UnsupportedOperationException exception) {
            showError(exception);
        }
    }

    private void review(String decision) {
        PrescriptionRecord record = selectedRecord();
        if (record == null) {
            return;
        }
        try {
            if (!prescriptionDAO.review(record.getId(), decision)) {
                JOptionPane.showMessageDialog(this,
                        "This prescription has already been reviewed.",
                        "Review prescription", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (SQLException | IllegalArgumentException exception) {
            showError(exception);
        }
    }

    private static Path uploadDirectory() {
        String configured = System.getProperty("pharmacy.upload.dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PHARMACY_UPLOAD_DIR");
        }
        if (configured == null || configured.isBlank()) {
            configured = Path.of(System.getProperty("java.io.tmpdir"),
                    "online-pharmacy-prescriptions").toString();
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    private void showError(Throwable error) {
        System.getLogger(PrescriptionPanel.class.getName()).log(System.Logger.Level.ERROR,
                "Prescription operation failed.", error);
        JOptionPane.showMessageDialog(this,
                "The prescription file could not be opened or reviewed.",
                "Prescription error", JOptionPane.ERROR_MESSAGE);
    }
}
