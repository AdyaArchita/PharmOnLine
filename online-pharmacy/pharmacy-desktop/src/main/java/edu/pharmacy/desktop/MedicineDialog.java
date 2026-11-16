package edu.pharmacy.desktop;

import edu.pharmacy.model.Medicine;
import edu.pharmacy.model.OTCMedicine;
import edu.pharmacy.model.PrescriptionMedicine;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/** Modal form for adding or editing a medicine. */
public class MedicineDialog extends JDialog {
    private final Medicine original;
    private final JTextField nameField = new JTextField(24);
    private final JTextField descriptionField = new JTextField(24);
    private final JTextField categoryField = new JTextField(24);
    private final JTextField supplierField = new JTextField(24);
    private final JTextField priceField = new JTextField(24);
    private final JTextField stockField = new JTextField(24);
    private final JTextField expiryField = new JTextField(24);
    private final JCheckBox prescriptionBox = new JCheckBox("Prescription required");
    private Medicine result;

    public MedicineDialog(java.awt.Window owner, Medicine medicine) {
        super(owner, medicine == null ? "Add medicine" : "Edit medicine",
                ModalityType.APPLICATION_MODAL);
        original = medicine;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        buildForm();
        if (medicine != null) {
            populate(medicine);
        } else {
            expiryField.setText(LocalDate.now().plusYears(2).toString());
            stockField.setText("0");
            priceField.setText("0.00");
        }
        pack();
        setLocationRelativeTo(owner);
    }

    public Medicine getResult() {
        return result;
    }

    private void buildForm() {
        JPanel fields = new JPanel(new GridBagLayout());
        addRow(fields, "Name:", nameField, 0);
        addRow(fields, "Description:", descriptionField, 1);
        addRow(fields, "Category ID:", categoryField, 2);
        addRow(fields, "Supplier ID (optional):", supplierField, 3);
        addRow(fields, "Unit price:", priceField, 4);
        addRow(fields, "Stock:", stockField, 5);
        addRow(fields, "Expiry (YYYY-MM-DD):", expiryField, 6);
        GridBagConstraints check = new GridBagConstraints();
        check.gridx = 1;
        check.gridy = 7;
        check.anchor = GridBagConstraints.LINE_START;
        check.insets = new Insets(5, 5, 5, 5);
        fields.add(prescriptionBox, check);
        add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton save = new JButton("Save");
        save.addActionListener(event -> saveMedicine());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(event -> dispose());
        buttons.add(save);
        buttons.add(cancel);
        add(buttons, BorderLayout.SOUTH);
    }

    private static void addRow(JPanel panel, String label, java.awt.Component field, int row) {
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

    private void populate(Medicine medicine) {
        nameField.setText(medicine.getName());
        descriptionField.setText(medicine.getDescription());
        categoryField.setText(Long.toString(medicine.getCategoryId()));
        supplierField.setText(medicine.getSupplierId() == null
                ? "" : medicine.getSupplierId().toString());
        priceField.setText(medicine.getUnitPrice().toPlainString());
        stockField.setText(Integer.toString(medicine.getStock()));
        expiryField.setText(medicine.getExpiryDate().toString());
        prescriptionBox.setSelected(medicine.isPrescriptionRequired());
    }

    private void saveMedicine() {
        try {
            long id = original == null ? 0 : original.getId();
            long categoryId = Long.parseLong(categoryField.getText().trim());
            String supplierText = supplierField.getText().trim();
            Long supplierId = supplierText.isEmpty() ? null : Long.valueOf(supplierText);
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());
            LocalDate expiry = LocalDate.parse(expiryField.getText().trim());
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();

            result = prescriptionBox.isSelected()
                    ? new PrescriptionMedicine(id, name, description, categoryId,
                            supplierId, price, stock, expiry)
                    : new OTCMedicine(id, name, description, categoryId,
                            supplierId, price, stock, expiry);
            dispose();
        } catch (NumberFormatException | java.time.format.DateTimeParseException exception) {
            showValidationError("Enter numeric ids, price and stock, and an expiry date "
                    + "in YYYY-MM-DD format.");
        } catch (IllegalArgumentException exception) {
            showValidationError(exception.getMessage());
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Check medicine details",
                JOptionPane.WARNING_MESSAGE);
    }
}
