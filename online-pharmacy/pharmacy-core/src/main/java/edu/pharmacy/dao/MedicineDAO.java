package edu.pharmacy.dao;

import edu.pharmacy.model.Medicine;
import edu.pharmacy.model.OTCMedicine;
import edu.pharmacy.model.PrescriptionMedicine;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC CRUD operations for medicines. All values are bound parameters. */
public class MedicineDAO {
    private static final String COLUMNS =
            "medicine_id, name, description, category_id, supplier_id, unit_price, "
                    + "stock, expiry_date, requires_prescription";

    public long create(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines "
                + "(name, description, category_id, supplier_id, unit_price, stock, "
                + "expiry_date, requires_prescription) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindMedicine(statement, medicine);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the new medicine id.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean update(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, description = ?, category_id = ?, "
                + "supplier_id = ?, unit_price = ?, stock = ?, expiry_date = ?, "
                + "requires_prescription = ? WHERE medicine_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindMedicine(statement, medicine);
            statement.setLong(9, medicine.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, medicineId);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<Medicine> findById(long medicineId) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM medicines WHERE medicine_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, medicineId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapMedicine(result)) : Optional.empty();
            }
        }
    }

    public List<Medicine> findAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM medicines ORDER BY name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return mapMedicines(result);
        }
    }

    public List<Medicine> search(String query) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM medicines "
                + "WHERE name LIKE ? OR description LIKE ? ORDER BY name";
        String term = "%" + (query == null ? "" : query.trim()) + "%";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, term);
            statement.setString(2, term);
            try (ResultSet result = statement.executeQuery()) {
                return mapMedicines(result);
            }
        }
    }

    private static void bindMedicine(PreparedStatement statement, Medicine medicine)
            throws SQLException {
        statement.setString(1, medicine.getName());
        statement.setString(2, medicine.getDescription());
        statement.setLong(3, medicine.getCategoryId());
        if (medicine.getSupplierId() == null) {
            statement.setNull(4, java.sql.Types.BIGINT);
        } else {
            statement.setLong(4, medicine.getSupplierId());
        }
        statement.setBigDecimal(5, medicine.getUnitPrice());
        statement.setInt(6, medicine.getStock());
        statement.setDate(7, Date.valueOf(medicine.getExpiryDate()));
        statement.setBoolean(8, medicine.isPrescriptionRequired());
    }

    private static List<Medicine> mapMedicines(ResultSet result) throws SQLException {
        List<Medicine> medicines = new ArrayList<>();
        while (result.next()) {
            medicines.add(mapMedicine(result));
        }
        return medicines;
    }

    private static Medicine mapMedicine(ResultSet result) throws SQLException {
        long supplierKey = result.getLong("supplier_id");
        Long supplierId = result.wasNull() ? null : supplierKey;
        boolean prescriptionRequired = result.getBoolean("requires_prescription");
        long id = result.getLong("medicine_id");
        String name = result.getString("name");
        String description = result.getString("description");
        long categoryId = result.getLong("category_id");
        var price = result.getBigDecimal("unit_price");
        int stock = result.getInt("stock");
        var expiryDate = result.getDate("expiry_date").toLocalDate();
        if (prescriptionRequired) {
            return new PrescriptionMedicine(id, name, description, categoryId, supplierId,
                    price, stock, expiryDate);
        }
        return new OTCMedicine(id, name, description, categoryId, supplierId,
                price, stock, expiryDate);
    }
}
