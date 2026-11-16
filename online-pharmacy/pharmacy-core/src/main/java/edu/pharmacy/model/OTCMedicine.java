package edu.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Over-the-counter medicine that does not require a prescription. */
public final class OTCMedicine extends Medicine {
    public OTCMedicine(long id, String name, String description, long categoryId,
                       Long supplierId, BigDecimal unitPrice, int stock,
                       LocalDate expiryDate) {
        super(id, name, description, categoryId, supplierId, unitPrice, stock,
                expiryDate, false);
    }
}
