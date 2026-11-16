package edu.pharmacy.dao;

import edu.pharmacy.model.Admin;
import edu.pharmacy.model.Customer;
import edu.pharmacy.model.Person;
import edu.pharmacy.model.Pharmacist;
import edu.pharmacy.model.Role;
import edu.pharmacy.security.PasswordHasher;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;

/** JDBC CRUD and login operations for users. Passwords are never stored as plain text. */
public class UserDAO {
    private static final String USER_COLUMNS =
            "user_id, full_name, email, password_hash, password_salt, role, "
                    + "address, license_number, department";

    public long create(Person user, char[] password)
            throws SQLException, GeneralSecurityException {
        PasswordHasher.Hash hash = PasswordHasher.create(password);
        String sql = "INSERT INTO users (full_name, email, password_hash, password_salt, "
                + "role, address, license_number, department) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindUser(statement, user);
            statement.setString(3, hash.value());
            statement.setString(4, hash.salt());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the new user id.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean update(Person user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, address = ?, "
                + "license_number = ?, department = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFullName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole().name());
            bindRoleFields(statement, user, 4);
            statement.setLong(7, user.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean updatePassword(long userId, char[] newPassword)
            throws SQLException, GeneralSecurityException {
        PasswordHasher.Hash hash = PasswordHasher.create(newPassword);
        String sql = "UPDATE users SET password_hash = ?, password_salt = ? WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, hash.value());
            statement.setString(2, hash.salt());
            statement.setLong(3, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate() == 1;
        }
    }

    public Optional<Person> findById(long userId) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapUser(result)) : Optional.empty();
            }
        }
    }

    public Optional<Person> findByEmail(String email) throws SQLException {
        String sql = "SELECT " + USER_COLUMNS + " FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim().toLowerCase(Locale.ROOT));
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapUser(result)) : Optional.empty();
            }
        }
    }

    public Optional<Person> authenticate(String email, char[] password)
            throws SQLException, GeneralSecurityException {
        Optional<Person> user = findByEmail(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        Person person = user.get();
        return PasswordHasher.verify(password, person.getPasswordSalt(), person.getPasswordHash())
                ? user : Optional.empty();
    }

    private static void bindUser(PreparedStatement statement, Person user) throws SQLException {
        statement.setString(1, user.getFullName());
        statement.setString(2, user.getEmail());
        statement.setString(5, user.getRole().name());
        bindRoleFields(statement, user, 6);
    }

    private static void bindRoleFields(PreparedStatement statement, Person user, int start)
            throws SQLException {
        if (user instanceof Customer customer) {
            statement.setString(start, customer.getAddress());
            statement.setNull(start + 1, java.sql.Types.VARCHAR);
            statement.setNull(start + 2, java.sql.Types.VARCHAR);
        } else if (user instanceof Pharmacist pharmacist) {
            statement.setNull(start, java.sql.Types.VARCHAR);
            statement.setString(start + 1, pharmacist.getLicenseNumber());
            statement.setNull(start + 2, java.sql.Types.VARCHAR);
        } else if (user instanceof Admin admin) {
            statement.setNull(start, java.sql.Types.VARCHAR);
            statement.setNull(start + 1, java.sql.Types.VARCHAR);
            statement.setString(start + 2, admin.getDepartment());
        } else {
            throw new IllegalArgumentException("Unsupported user type: " + user.getClass());
        }
    }

    private static Person mapUser(ResultSet result) throws SQLException {
        long id = result.getLong("user_id");
        String name = result.getString("full_name");
        String email = result.getString("email");
        String hash = result.getString("password_hash");
        String salt = result.getString("password_salt");
        Role role = Role.valueOf(result.getString("role"));
        return switch (role) {
            case CUSTOMER -> new Customer(id, name, email, result.getString("address"), hash, salt);
            case PHARMACIST -> new Pharmacist(id, name, email,
                    result.getString("license_number"), hash, salt);
            case ADMIN -> new Admin(id, name, email, result.getString("department"), hash, salt);
        };
    }
}
