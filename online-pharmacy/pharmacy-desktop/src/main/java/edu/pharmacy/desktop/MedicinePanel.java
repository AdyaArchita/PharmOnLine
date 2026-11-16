package edu.pharmacy.desktop;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.model.Medicine;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;

/** Medicine catalog, category tree, search, and add/edit/delete actions. */
public class MedicinePanel extends JPanel implements DashboardPanel.Refreshable {
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final MedicineTableModel tableModel = new MedicineTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(24);
    private final JButton editButton = new JButton("Edit selected");
    private final JButton deleteButton = new JButton("Delete selected");
    private List<Medicine> allMedicines = new ArrayList<>();
    private Long selectedCategoryId;

    public MedicinePanel() {
        setLayout(new BorderLayout(8, 8));
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildCatalog(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                filterRows();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                filterRows();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                filterRows();
            }
        });
        refresh();
    }

    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(new JLabel("Search medicines:"));
        panel.add(searchField);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        panel.add(refreshButton);
        return panel;
    }

    private JSplitPane buildCatalog() {
        JTree categories = createCategoryTree();
        categories.addTreeSelectionListener(event -> {
            Object selected = ((DefaultMutableTreeNode) event.getPath().getLastPathComponent())
                    .getUserObject();
            if (selected instanceof CategorySelection category) {
                selectedCategoryId = category.categoryId();
            } else {
                selectedCategoryId = null;
            }
            filterRows();
        });
        JScrollPane treeScroll = new JScrollPane(categories);
        treeScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Categories"));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Medicines"));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, tableScroll);
        split.setResizeWeight(0.22);
        return split;
    }

    private static JTree createCategoryTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("All medicines");
        DefaultMutableTreeNode antibiotics = category("Antibiotics", 2);
        antibiotics.add(category("Penicillins", 3));
        antibiotics.add(category("Macrolides", 4));
        root.add(antibiotics);
        DefaultMutableTreeNode painkillers = category("Painkillers", 5);
        painkillers.add(category("Analgesics", 6));
        root.add(painkillers);
        root.add(category("Fever and Cold", 7));
        root.add(category("Vitamins and Supplements", 8));
        root.add(category("Digestive Health", 9));
        root.add(category("First Aid", 10));
        return new JTree(new DefaultTreeModel(root));
    }

    private static DefaultMutableTreeNode category(String name, long categoryId) {
        return new DefaultMutableTreeNode(new CategorySelection(name, categoryId));
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JButton addButton = new JButton("Add medicine");
        addButton.addActionListener(event -> editMedicine(null));
        editButton.addActionListener(event -> editMedicine(selectedMedicine()));
        deleteButton.addActionListener(event -> deleteMedicine());
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);
        return actions;
    }

    @Override
    public void refresh() {
        new SwingWorker<List<Medicine>, Void>() {
            @Override
            protected List<Medicine> doInBackground() throws SQLException {
                return medicineDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    allMedicines = get();
                    filterRows();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError(exception);
                } catch (ExecutionException exception) {
                    showError(exception.getCause());
                }
            }
        }.execute();
    }

    private void filterRows() {
        String query = searchField.getText().trim();
        List<Medicine> filtered = allMedicines.stream()
                .filter(medicine -> medicine.matches(query))
                .filter(this::matchesSelectedCategory)
                .toList();
        tableModel.setRows(filtered);
    }

    private boolean matchesSelectedCategory(Medicine medicine) {
        if (selectedCategoryId == null) {
            return true;
        }
        long categoryId = selectedCategoryId;
        if (categoryId == 2) {
            return medicine.getCategoryId() == 2 || medicine.getCategoryId() == 3
                    || medicine.getCategoryId() == 4;
        }
        if (categoryId == 5) {
            return medicine.getCategoryId() == 5 || medicine.getCategoryId() == 6;
        }
        return medicine.getCategoryId() == categoryId;
    }

    private void selectionChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        }
    }

    private Medicine selectedMedicine() {
        int selectedRow = table.getSelectedRow();
        return selectedRow < 0 ? null
                : tableModel.getMedicineAt(table.convertRowIndexToModel(selectedRow));
    }

    private void editMedicine(Medicine current) {
        MedicineDialog dialog = new MedicineDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), current);
        dialog.setVisible(true);
        Medicine updated = dialog.getResult();
        if (updated == null) {
            return;
        }
        try {
            if (current == null) {
                medicineDAO.create(updated);
            } else if (!medicineDAO.update(updated)) {
                JOptionPane.showMessageDialog(this, "The medicine was not found.",
                        "Save medicine", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void deleteMedicine() {
        Medicine medicine = selectedMedicine();
        if (medicine == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete " + medicine.getName() + "?", "Confirm deletion",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!medicineDAO.delete(medicine.getId())) {
                JOptionPane.showMessageDialog(this, "The medicine was not found.",
                        "Delete medicine", JOptionPane.WARNING_MESSAGE);
            }
            refresh();
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void showError(Throwable error) {
        System.getLogger(MedicinePanel.class.getName()).log(System.Logger.Level.ERROR,
                "Medicine operation failed.", error);
        JOptionPane.showMessageDialog(this, "Medicine data could not be loaded or saved.",
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private record CategorySelection(String name, long categoryId) {
        @Override
        public String toString() {
            return name;
        }
    }
}
