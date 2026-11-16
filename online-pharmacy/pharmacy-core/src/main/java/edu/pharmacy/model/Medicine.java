package edu.pharmacy.model;

import edu.pharmacy.exception.ExpiredMedicineException;
import edu.pharmacy.exception.InvalidPrescriptionException;
import edu.pharmacy.exception.OutOfStockException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/** Common medicine data and the shared sale validation rules. */
public abstract class Medicine implements Billable, Searchable, Reportable {
    private final long id;
    private final String name;
    private final String description;
    private final long categoryId;
    private final Long supplierId;
    private final BigDecimal unitPrice;
    private int stock;
    private final LocalDate expiryDate;
    private final boolean prescriptionRequired;

    protected Medicine(long id, String name, String description, long categoryId,
                       Long supplierId, BigDecimal unitPrice, int stock,
                       LocalDate expiryDate, boolean prescriptionRequired) {
        if (id < 0 || categoryId <= 0 || (supplierId != null && supplierId <= 0)) {
            throw new IllegalArgumentException("Invalid medicine, category, or supplier id.");
        }
        this.id = id;
        this.name = requireText(name, "Medicine name");
        this.description = description == null ? "" : description.trim();
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price is required.");
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.stock = stock;
        this.expiryDate = Objects.requireNonNull(expiryDate, "Expiry date is required.");
        this.prescriptionRequired = prescriptionRequired;
    }

    private static String requireText(String value, String label) {
        Objects.requireNonNull(value, label + " is required.");
        String cleaned = value.trim();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty.");
        }
        return cleaned;
    }

    public void validateForSale(int quantity, boolean approvedPrescription)
            throws OutOfStockException, ExpiredMedicineException, InvalidPrescriptionException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (expiryDate.isBefore(LocalDate.now())) {
            throw new ExpiredMedicineException(name + " expired on " + expiryDate + ".");
        }
        if (quantity > stock) {
            throw new OutOfStockException("Only " + stock + " unit(s) of " + name + " remain.");
        }
        if (prescriptionRequired && !approvedPrescription) {
            throw new InvalidPrescriptionException(
                    "An approved prescription is required for " + name + ".");
        }
    }

    /** Keeps in-memory stock safe if multiple worker threads share this instance. */
    public synchronized void removeStock(int quantity) throws OutOfStockException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (quantity > stock) {
            throw new OutOfStockException("Cannot remove more stock than is available.");
        }
        stock -= quantity;
    }

    public synchronized void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        stock = Math.addExact(stock, quantity);
    }

    @Override
    public BigDecimal calculateTotal(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String term = query.trim().toLowerCase(Locale.ROOT);
        return name.toLowerCase(Locale.ROOT).contains(term)
                || description.toLowerCase(Locale.ROOT).contains(term);
    }

    @Override
    public String toReportLine() {
        return id + " | " + name + " | stock=" + stock + " | unit price=" + unitPrice;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public synchronized int getStock() {
        return stock;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }
}
