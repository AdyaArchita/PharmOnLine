package edu.pharmacy.desktop;

import edu.pharmacy.model.Medicine;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Table adapter that presents medicines without putting database code in the UI. */
public class MedicineTableModel extends AbstractTableModel {
    private static final String[] COLUMNS =
            {"ID", "Name", "Category", "Supplier", "Price", "Stock", "Expiry", "Rx"};
    private List<Medicine> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Medicine medicine = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> medicine.getId();
            case 1 -> medicine.getName();
            case 2 -> medicine.getCategoryId();
            case 3 -> medicine.getSupplierId() == null ? "" : medicine.getSupplierId();
            case 4 -> medicine.getUnitPrice();
            case 5 -> medicine.getStock();
            case 6 -> medicine.getExpiryDate();
            case 7 -> medicine.isPrescriptionRequired() ? "Required" : "No";
            default -> throw new IndexOutOfBoundsException("Unknown medicine column.");
        };
    }

    public Medicine getMedicineAt(int row) {
        return rows.get(row);
    }

    public void setRows(List<Medicine> medicines) {
        rows = new ArrayList<>(medicines);
        fireTableDataChanged();
    }
}
