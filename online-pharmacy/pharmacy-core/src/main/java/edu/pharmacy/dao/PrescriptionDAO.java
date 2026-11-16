package edu.pharmacy.dao;

import edu.pharmacy.model.PrescriptionRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Stores private prescription-file references and returns their review status. */
public class PrescriptionDAO {
    public long createPending(long customerId, long medicineId, String storedFileName)
            throws SQLException {
        String sql = "INSERT INTO prescriptions "
                + "(customer_id, medicine_id, file_path, status) "
                + "SELECT ?, m.medicine_id, ?, 'PENDING' FROM medicines m "
                + "JOIN users u ON u.user_id = ? "
                + "WHERE m.medicine_id = ? AND m.requires_prescription = TRUE "
                + "AND u.role = 'CUSTOMER'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setString(2, storedFileName);
            statement.setLong(3, customerId);
            statement.setLong(4, medicineId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Prescription upload does not match a customer "
                        + "and prescription-only medicine.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the prescription id.");
                }
                return keys.getLong(1);
            }
        }
    }

    public Optional<String> findStatus(long customerId, long prescriptionId)
            throws SQLException {
        String sql = "SELECT status FROM prescriptions "
                + "WHERE customer_id = ? AND prescription_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            statement.setLong(2, prescriptionId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(result.getString("status")) : Optional.empty();
            }
        }
    }

    public List<PrescriptionRecord> findPending() throws SQLException {
        String sql = "SELECT prescription_id, customer_id, medicine_id, file_path, "
                + "status, uploaded_at FROM prescriptions "
                + "WHERE status = 'PENDING' ORDER BY uploaded_at";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<PrescriptionRecord> records = new ArrayList<>();
            while (result.next()) {
                Timestamp uploaded = result.getTimestamp("uploaded_at");
                records.add(new PrescriptionRecord(result.getLong("prescription_id"),
                        result.getLong("customer_id"), result.getLong("medicine_id"),
                        result.getString("file_path"), result.getString("status"),
                        uploaded.toLocalDateTime()));
            }
            return records;
        }
    }

    public boolean review(long prescriptionId, String decision) throws SQLException {
        if (prescriptionId <= 0 || decision == null
                || !decision.equals("APPROVED") && !decision.equals("REJECTED")) {
            throw new IllegalArgumentException("Invalid prescription review decision.");
        }
        String sql = "UPDATE prescriptions SET status = ?, reviewed_at = CURRENT_TIMESTAMP "
                + "WHERE prescription_id = ? AND status = 'PENDING'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, decision);
            statement.setLong(2, prescriptionId);
            return statement.executeUpdate() == 1;
        }
    }
}
