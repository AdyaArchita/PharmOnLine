package edu.pharmacy.desktop;

import edu.pharmacy.model.Supplier;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/** Small modal editor for supplier contact information. */
public class SupplierDialog extends JDialog {
    private final Supplier original;
    private final JTextField nameField = new JTextField(24);
    private final JTextField contactField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JTextField addressField = new JTextField(24);
    private Supplier result;

    public SupplierDialog(java.awt.Window owner, Supplier supplier) {
        super(owner, supplier == null ? "Add supplier" : "Edit supplier",
                ModalityType.APPLICATION_MODAL);
        original = supplier;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        buildForm();
        if (supplier != null) {
            nameField.setText(supplier.getName());
            contactField.setText(supplier.getContactName());
            phoneField.setText(supplier.getPhone());
            emailField.setText(supplier.getEmail());
            addressField.setText(supplier.getAddress());
        }
        pack();
        setLocationRelativeTo(owner);
    }

    public Supplier getResult() {
        return result;
    }

    private void buildForm() {
        JPanel fields = new JPanel(new GridBagLayout());
        addRow(fields, "Supplier name:", nameField, 0);
        addRow(fields, "Contact name:", contactField, 1);
        addRow(fields, "Phone:", phoneField, 2);
        addRow(fields, "Email:", emailField, 3);
        addRow(fields, "Address:", addressField, 4);
        add(fields);

        JButton save = new JButton("Save");
        save.addActionListener(event -> saveSupplier());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(event -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(save);
        buttons.add(cancel);
        add(buttons, java.awt.BorderLayout.SOUTH);
    }

    private static void addRow(JPanel panel, String label, JTextField field, int row) {
        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = row;
        left.anchor = GridBagConstraints.LINE_END;
        left.insets = new Insets(5, 5, 5, 5);
        panel.add(new JLabel(label), left);
        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = row;
        right.weightx = 1;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.insets = new Insets(5, 5, 5, 5);
        panel.add(field, right);
    }

    private void saveSupplier() {
        try {
            long id = original == null ? 0 : original.getId();
            result = new Supplier(id, nameField.getText(), contactField.getText(),
                    phoneField.getText(), emailField.getText(), addressField.getText());
            dispose();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Check supplier details", JOptionPane.WARNING_MESSAGE);
        }
    }
}
