package edu.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Medicine category that can only be sold with an approved prescription. */
public final class PrescriptionMedicine extends Medicine {
    public PrescriptionMedicine(long id, String name, String description, long categoryId,
                                Long supplierId, BigDecimal unitPrice, int stock,
                                LocalDate expiryDate) {
        super(id, name, description, categoryId, supplierId, unitPrice, stock,
                expiryDate, true);
    }
}
