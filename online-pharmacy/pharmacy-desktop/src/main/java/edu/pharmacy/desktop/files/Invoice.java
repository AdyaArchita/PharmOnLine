package edu.pharmacy.desktop.files;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Serializable invoice snapshot saved by the staff application. */
public final class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long orderId;
    private final long customerId;
    private final LocalDateTime createdAt;
    private final List<InvoiceLine> lines;
    private final BigDecimal totalAmount;

    public Invoice(long orderId, long customerId, LocalDateTime createdAt,
                   List<InvoiceLine> lines, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.lines = new ArrayList<>(lines);
        this.totalAmount = totalAmount;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<InvoiceLine> getLines() {
        return List.copyOf(lines);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
