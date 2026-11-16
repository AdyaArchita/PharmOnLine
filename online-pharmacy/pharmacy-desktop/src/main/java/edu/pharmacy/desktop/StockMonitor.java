package edu.pharmacy.desktop;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.model.Medicine;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Periodically logs low-stock and near-expiry medicines on a background thread. */
public class StockMonitor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(StockMonitor.class.getName());

    private final MedicineDAO medicineDAO;
    private final int lowStockThreshold;
    private final int nearExpiryDays;
    private final long intervalSeconds;

    public StockMonitor(MedicineDAO medicineDAO, int lowStockThreshold,
                        int nearExpiryDays, long intervalSeconds) {
        if (lowStockThreshold < 0 || nearExpiryDays < 0 || intervalSeconds <= 0) {
            throw new IllegalArgumentException("Monitor thresholds and interval are invalid.");
        }
        this.medicineDAO = medicineDAO;
        this.lowStockThreshold = lowStockThreshold;
        this.nearExpiryDays = nearExpiryDays;
        this.intervalSeconds = intervalSeconds;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            checkInventory();
            try {
                Thread.sleep(intervalSeconds * 1_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void checkInventory() {
        LocalDate today = LocalDate.now();
        LocalDate nearExpiry = today.plusDays(nearExpiryDays);
        try {
            for (Medicine medicine : medicineDAO.findAll()) {
                if (medicine.getStock() <= lowStockThreshold) {
                    LOGGER.warning(() -> "LOW STOCK: " + medicine.getName()
                            + " has " + medicine.getStock() + " unit(s).");
                }
                if (!medicine.getExpiryDate().isBefore(today)
                        && !medicine.getExpiryDate().isAfter(nearExpiry)) {
                    LOGGER.warning(() -> "NEAR EXPIRY: " + medicine.getName()
                            + " expires on " + medicine.getExpiryDate() + ".");
                }
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Stock monitor could not read inventory.", exception);
        }
    }
}
