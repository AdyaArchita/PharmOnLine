package edu.pharmacy.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Creates JDBC connections from environment variables or JVM system properties. */
public final class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/online_pharmacy?useSSL=false&serverTimezone=UTC";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = setting("PHARMACY_DB_URL", "pharmacy.db.url", DEFAULT_URL);
        String user = setting("PHARMACY_DB_USER", "pharmacy.db.user", "root");
        String password = setting("PHARMACY_DB_PASSWORD", "pharmacy.db.password", null);
        if (password == null) {
            throw new SQLException("Set PHARMACY_DB_PASSWORD or -Dpharmacy.db.password.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String environmentName, String propertyName, String fallback) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentName);
        }
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
