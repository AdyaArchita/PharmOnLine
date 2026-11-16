package edu.pharmacy.model;

import java.math.BigDecimal;

/** Something that can calculate a price for a requested quantity. */
public interface Billable {
    BigDecimal calculateTotal(int quantity);
}
