package edu.pharmacy.desktop;

import edu.pharmacy.model.Supplier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Table adapter for supplier contact details. */
public class SupplierTableModel extends AbstractTableModel {
    private static final String[] COLUMNS =
            {"ID", "Supplier", "Contact", "Phone", "Email", "Address"};
    private List<Supplier> rows = new ArrayList<>();

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
        Supplier supplier = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> supplier.getId();
            case 1 -> supplier.getName();
            case 2 -> supplier.getContactName();
            case 3 -> supplier.getPhone();
            case 4 -> supplier.getEmail();
            case 5 -> supplier.getAddress();
            default -> throw new IndexOutOfBoundsException("Unknown supplier column.");
        };
    }

    public Supplier getSupplierAt(int row) {
        return rows.get(row);
    }

    public void setRows(List<Supplier> suppliers) {
        rows = new ArrayList<>(suppliers);
        fireTableDataChanged();
    }
}
