package edu.pharmacy.desktop.files;

import java.nio.file.Path;

/** Resolves the private output directory used for report and invoice files. */
public final class ReportFiles {
    private ReportFiles() {
    }

    public static Path directory() {
        String configured = System.getProperty("pharmacy.report.dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PHARMACY_REPORT_DIR");
        }
        if (configured == null || configured.isBlank()) {
            configured = Path.of(System.getProperty("user.home"),
                    "online-pharmacy-reports").toString();
        }
        return Path.of(configured).toAbsolutePath().normalize();
    }

    public static Path dailySalesReport() {
        return directory().resolve("daily-sales-" + java.time.LocalDate.now() + ".txt");
    }

    public static Path invoiceFile(long orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be greater than zero.");
        }
        return directory().resolve("invoice-" + orderId + ".bin");
    }
}
