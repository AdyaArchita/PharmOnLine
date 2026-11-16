package edu.pharmacy.model;

import java.math.BigDecimal;

/** Persisted order line details used to create a printable or archived invoice. */
public final class OrderLine {
    private final long medicineId;
    private final String medicineName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderLine(long medicineId, String medicineName, int quantity, BigDecimal unitPrice) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
