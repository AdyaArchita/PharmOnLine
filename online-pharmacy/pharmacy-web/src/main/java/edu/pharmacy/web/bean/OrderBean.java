package edu.pharmacy.web.bean;

import java.math.BigDecimal;
import java.util.Date;

/** JavaBean order summary displayed in the customer's order history. */
public class OrderBean {
    private long id;
    private Date createdAt;
    private String status;
    private BigDecimal totalAmount;

    public OrderBean() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
