package edu.pharmacy.desktop;

import edu.pharmacy.model.CustomerOrder;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/** Readable order summaries for the orders tab. */
public class OrderTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Order", "Customer", "Placed", "Status", "Total"};
    private List<CustomerOrder> rows = new ArrayList<>();

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
        CustomerOrder order = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> order.getId();
            case 1 -> order.getCustomerId();
            case 2 -> order.getCreatedAt();
            case 3 -> order.getStatus();
            case 4 -> order.getTotalAmount();
            default -> throw new IndexOutOfBoundsException("Unknown order column.");
        };
    }

    public CustomerOrder getOrderAt(int row) {
        return rows.get(row);
    }

    public void setRows(List<CustomerOrder> orders) {
        rows = new ArrayList<>(orders);
        fireTableDataChanged();
    }
}
