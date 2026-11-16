package edu.pharmacy.model;

/** A requested line item; price is always read from the database during checkout. */
public final class OrderItem {
    private final long medicineId;
    private final int quantity;
    private final Long prescriptionId;

    public OrderItem(long medicineId, int quantity, Long prescriptionId) {
        if (medicineId <= 0 || quantity <= 0 || (prescriptionId != null && prescriptionId <= 0)) {
            throw new IllegalArgumentException("Invalid medicine, quantity, or prescription id.");
        }
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.prescriptionId = prescriptionId;
    }

    public long getMedicineId() {
        return medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }
}
