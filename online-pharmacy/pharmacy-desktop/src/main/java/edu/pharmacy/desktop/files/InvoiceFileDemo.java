package edu.pharmacy.desktop.files;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/** Small command-line demonstration of saving and loading a binary invoice. */
public final class InvoiceFileDemo {
    private InvoiceFileDemo() {
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Path file = args.length == 0
                ? ReportFiles.invoiceFile(1) : Path.of(args[0]).toAbsolutePath().normalize();
        Invoice example = new Invoice(1, 1, LocalDateTime.now(),
                List.of(new InvoiceLine(6, "Paracetamol 500 mg", 2,
                        new BigDecimal("2.50"))), new BigDecimal("5.00"));
        InvoiceFileStore.save(example, file);
        Invoice loaded = InvoiceFileStore.load(file);
        System.out.println("Loaded invoice " + loaded.getOrderId() + " from " + file);
        System.out.println("Line count: " + loaded.getLines().size()
                + ", total: INR " + loaded.getTotalAmount());
    }
}
