package edu.pharmacy.desktop;

import edu.pharmacy.model.PrescriptionRecord;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Table adapter for prescriptions waiting on pharmacist review. */
public class PrescriptionTableModel extends AbstractTableModel {
    private static final String[] COLUMNS =
            {"ID", "Customer", "Medicine", "File", "Status", "Uploaded"};
    private List<PrescriptionRecord> rows = new ArrayList<>();

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
        PrescriptionRecord record = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> record.getId();
            case 1 -> record.getCustomerId();
            case 2 -> record.getMedicineId();
            case 3 -> record.getFileName();
            case 4 -> record.getStatus();
            case 5 -> record.getUploadedAt();
            default -> throw new IndexOutOfBoundsException("Unknown prescription column.");
        };
    }

    public PrescriptionRecord getRecordAt(int row) {
        return rows.get(row);
    }

    public void setRows(List<PrescriptionRecord> records) {
        rows = new ArrayList<>(records);
        fireTableDataChanged();
    }
}
