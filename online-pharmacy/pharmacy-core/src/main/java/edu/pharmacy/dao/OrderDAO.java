package edu.pharmacy.dao;

import edu.pharmacy.exception.ExpiredMedicineException;
import edu.pharmacy.exception.InvalidPrescriptionException;
import edu.pharmacy.exception.OutOfStockException;
import edu.pharmacy.model.CustomerOrder;
import edu.pharmacy.model.OrderItem;
import edu.pharmacy.model.OrderLine;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Order creation and history queries. Stock deductions are part of the same transaction. */
public class OrderDAO {
    private static final Set<String> ALLOWED_STATUSES =
            Set.of("PLACED", "PROCESSING", "READY", "COMPLETED", "CANCELLED");

    public CustomerOrder placeOrder(long customerId, List<OrderItem> items)
            throws SQLException, OutOfStockException, ExpiredMedicineException,
            InvalidPrescriptionException {
        if (customerId <= 0 || items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A customer and at least one item are required.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                BigDecimal total = BigDecimal.ZERO;
                for (OrderItem item : items) {
                    total = total.add(validateAndDeductStock(connection, customerId, item));
                }

                long orderId = insertOrder(connection, customerId, total);
                for (OrderItem item : items) {
                    insertOrderItem(connection, orderId, item);
                    if (item.getPrescriptionId() != null) {
                        linkPrescription(connection, item.getPrescriptionId(), orderId);
                    }
                }

                connection.commit();
                return new CustomerOrder(orderId, customerId, java.time.LocalDateTime.now(),
                        "PLACED", total);
            } catch (SQLException | OutOfStockException | ExpiredMedicineException
                     | InvalidPrescriptionException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public List<CustomerOrder> findByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT order_id, customer_id, created_at, status, total_amount "
                + "FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                List<CustomerOrder> orders = new ArrayList<>();
                while (result.next()) {
                    orders.add(mapOrder(result));
                }
                return orders;
            }
        }
    }

    public List<CustomerOrder> findAll() throws SQLException {
        String sql = "SELECT order_id, customer_id, created_at, status, total_amount "
                + "FROM orders ORDER BY created_at DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<CustomerOrder> orders = new ArrayList<>();
            while (result.next()) {
                orders.add(mapOrder(result));
            }
            return orders;
        }
    }

    public boolean updateStatus(long orderId, String status) throws SQLException {
        if (orderId <= 0 || status == null
                || !ALLOWED_STATUSES.contains(status.toUpperCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid order id or status.");
        }
        String newStatus = status.toUpperCase(java.util.Locale.ROOT);
        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String currentStatus = findStatusForUpdate(connection, orderId);
                if (currentStatus == null) {
                    connection.commit();
                    return false;
                }
                if (currentStatus.equals(newStatus)) {
                    connection.commit();
                    return true;
                }
                if (!canTransition(currentStatus, newStatus)) {
                    throw new IllegalArgumentException("Order cannot move from "
                            + currentStatus + " to " + newStatus + ".");
                }
                if ("CANCELLED".equals(newStatus)) {
                    restoreOrderStock(connection, orderId);
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE orders SET status = ? WHERE order_id = ?")) {
                    statement.setString(1, newStatus);
                    statement.setLong(2, orderId);
                    if (statement.executeUpdate() != 1) {
                        throw new SQLException("Order status was not updated.");
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException | IllegalArgumentException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private static String findStatusForUpdate(Connection connection, long orderId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM orders WHERE order_id = ? FOR UPDATE")) {
            statement.setLong(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("status") : null;
            }
        }
    }

    private static boolean canTransition(String current, String next) {
        if ("CANCELLED".equals(next)) {
            return !"COMPLETED".equals(current) && !"CANCELLED".equals(current);
        }
        return switch (current) {
            case "PLACED" -> "PROCESSING".equals(next);
            case "PROCESSING" -> "READY".equals(next);
            case "READY" -> "COMPLETED".equals(next);
            default -> false;
        };
    }

    private static void restoreOrderStock(Connection connection, long orderId)
            throws SQLException {
        String selectSql = "SELECT medicine_id, quantity FROM order_items WHERE order_id = ?";
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setLong(1, orderId);
            try (ResultSet result = select.executeQuery();
                 PreparedStatement update = connection.prepareStatement(
                         "UPDATE medicines SET stock = stock + ? WHERE medicine_id = ?")) {
                while (result.next()) {
                    update.setInt(1, result.getInt("quantity"));
                    update.setLong(2, result.getLong("medicine_id"));
                    if (update.executeUpdate() != 1) {
                        throw new SQLException("Could not restore stock for a cancelled order.");
                    }
                }
            }
        }
    }

    public Optional<CustomerOrder> findById(long orderId) throws SQLException {
        String sql = "SELECT order_id, customer_id, created_at, status, total_amount "
                + "FROM orders WHERE order_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapOrder(result)) : Optional.empty();
            }
        }
    }

    public List<OrderLine> findItemsByOrderId(long orderId) throws SQLException {
        String sql = "SELECT oi.medicine_id, m.name, oi.quantity, oi.unit_price "
                + "FROM order_items oi JOIN medicines m ON m.medicine_id = oi.medicine_id "
                + "WHERE oi.order_id = ? ORDER BY oi.order_item_id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            try (ResultSet result = statement.executeQuery()) {
                List<OrderLine> lines = new ArrayList<>();
                while (result.next()) {
                    lines.add(new OrderLine(result.getLong("medicine_id"),
                            result.getString("name"), result.getInt("quantity"),
                            result.getBigDecimal("unit_price")));
                }
                return lines;
            }
        }
    }

    private static BigDecimal validateAndDeductStock(Connection connection, long customerId,
                                                      OrderItem item)
            throws SQLException, OutOfStockException, ExpiredMedicineException,
            InvalidPrescriptionException {
        String selectSql = "SELECT unit_price, stock, expiry_date, requires_prescription "
                + "FROM medicines WHERE medicine_id = ? FOR UPDATE";
        BigDecimal unitPrice;
        try (PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setLong(1, item.getMedicineId());
            try (ResultSet result = select.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Medicine " + item.getMedicineId() + " does not exist.");
                }
                unitPrice = result.getBigDecimal("unit_price");
                int stock = result.getInt("stock");
                LocalDate expiry = result.getDate("expiry_date").toLocalDate();
                boolean prescriptionRequired = result.getBoolean("requires_prescription");
                if (expiry.isBefore(LocalDate.now())) {
                    throw new ExpiredMedicineException("Medicine " + item.getMedicineId()
                            + " expired on " + expiry + ".");
                }
                if (stock < item.getQuantity()) {
                    throw new OutOfStockException("Medicine " + item.getMedicineId()
                            + " has only " + stock + " unit(s) in stock.");
                }
                if (prescriptionRequired && !hasApprovedPrescription(
                        connection, customerId, item)) {
                    throw new InvalidPrescriptionException(
                            "An approved prescription is required for medicine "
                                    + item.getMedicineId() + ".");
                }
            }
        }

        String updateSql = "UPDATE medicines SET stock = stock - ? "
                + "WHERE medicine_id = ? AND stock >= ?";
        try (PreparedStatement update = connection.prepareStatement(updateSql)) {
            update.setInt(1, item.getQuantity());
            update.setLong(2, item.getMedicineId());
            update.setInt(3, item.getQuantity());
            if (update.executeUpdate() != 1) {
                throw new OutOfStockException("Stock changed while placing the order.");
            }
        }
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private static boolean hasApprovedPrescription(Connection connection, long customerId,
                                                    OrderItem item) throws SQLException {
        if (item.getPrescriptionId() == null) {
            return false;
        }
        String sql = "SELECT 1 FROM prescriptions WHERE prescription_id = ? "
                + "AND customer_id = ? AND medicine_id = ? AND status = 'APPROVED'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, item.getPrescriptionId());
            statement.setLong(2, customerId);
            statement.setLong(3, item.getMedicineId());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static long insertOrder(Connection connection, long customerId, BigDecimal total)
            throws SQLException {
        String sql = "INSERT INTO orders (customer_id, status, subtotal, discount_amount, "
                + "gst_amount, total_amount) VALUES (?, 'PLACED', ?, 0.00, 0.00, ?)";
        try (PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setBigDecimal(2, total);
            statement.setBigDecimal(3, total);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the new order id.");
                }
                return keys.getLong(1);
            }
        }
    }

    private static void insertOrderItem(Connection connection, long orderId, OrderItem item)
            throws SQLException {
        String sql = "INSERT INTO order_items "
                + "(order_id, medicine_id, quantity, unit_price, prescription_id) "
                + "SELECT ?, medicine_id, ?, unit_price, ? FROM medicines WHERE medicine_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setInt(2, item.getQuantity());
            if (item.getPrescriptionId() == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, item.getPrescriptionId());
            }
            statement.setLong(4, item.getMedicineId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Could not save an order item for medicine "
                        + item.getMedicineId() + ".");
            }
        }
    }

    private static void linkPrescription(Connection connection, long prescriptionId, long orderId)
            throws SQLException {
        String sql = "UPDATE prescriptions SET order_id = ? WHERE prescription_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, orderId);
            statement.setLong(2, prescriptionId);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Could not link prescription " + prescriptionId + ".");
            }
        }
    }

    private static CustomerOrder mapOrder(ResultSet result) throws SQLException {
        Timestamp created = result.getTimestamp("created_at");
        return new CustomerOrder(result.getLong("order_id"), result.getLong("customer_id"),
                created.toLocalDateTime(), result.getString("status"),
                result.getBigDecimal("total_amount"));
    }

    private static void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }
}
