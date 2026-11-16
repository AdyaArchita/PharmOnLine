package edu.pharmacy.desktop.files;

import java.io.Serializable;
import java.math.BigDecimal;

/** Serializable line item stored inside an archived invoice. */
public final class InvoiceLine implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long medicineId;
    private final String medicineName;
    private final int quantity;
    private final BigDecimal unitPrice;

    public InvoiceLine(long medicineId, String medicineName, int quantity, BigDecimal unitPrice) {
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

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
