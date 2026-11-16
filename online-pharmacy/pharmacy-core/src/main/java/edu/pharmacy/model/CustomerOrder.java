package edu.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Compact order summary returned by the order DAO. */
public final class CustomerOrder {
    private final long id;
    private final long customerId;
    private final LocalDateTime createdAt;
    private final String status;
    private final BigDecimal totalAmount;

    public CustomerOrder(long id, long customerId, LocalDateTime createdAt,
                         String status, BigDecimal totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
