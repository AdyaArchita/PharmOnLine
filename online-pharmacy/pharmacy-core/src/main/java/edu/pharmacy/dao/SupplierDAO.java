package edu.pharmacy.dao;

import edu.pharmacy.model.Supplier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** JDBC CRUD operations for pharmacy suppliers. */
public class SupplierDAO {
    public long create(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers "
                + "(name, contact_name, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindSupplier(statement, supplier);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the new supplier id.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean update(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, contact_name = ?, phone = ?, "
                + "email = ?, address = ? WHERE supplier_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindSupplier(statement, supplier);
            statement.setLong(6, supplier.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long supplierId) throws SQLException {
        String sql = "DELETE FROM suppliers WHERE supplier_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, supplierId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Supplier> findAll() throws SQLException {
        String sql = "SELECT supplier_id, name, contact_name, phone, email, address "
                + "FROM suppliers ORDER BY name";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Supplier> suppliers = new ArrayList<>();
            while (result.next()) {
                suppliers.add(new Supplier(result.getLong("supplier_id"),
                        result.getString("name"), result.getString("contact_name"),
                        result.getString("phone"), result.getString("email"),
                        result.getString("address")));
            }
            return suppliers;
        }
    }

    private static void bindSupplier(PreparedStatement statement, Supplier supplier)
            throws SQLException {
        statement.setString(1, supplier.getName());
        statement.setString(2, supplier.getContactName());
        statement.setString(3, supplier.getPhone());
        statement.setString(4, supplier.getEmail());
        statement.setString(5, supplier.getAddress());
    }
}
