package edu.pharmacy.desktop;

import edu.pharmacy.dao.OrderDAO;
import edu.pharmacy.desktop.files.ReportFiles;
import edu.pharmacy.model.CustomerOrder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Generates the completed sales report off the Swing event-dispatch thread. */
public class ReportGenerator extends Thread {
    private static final Logger LOGGER = Logger.getLogger(ReportGenerator.class.getName());
    private final OrderDAO orderDAO;

    public ReportGenerator() {
        this(new OrderDAO());
    }

    public ReportGenerator(OrderDAO orderDAO) {
        super("daily-sales-report");
        this.orderDAO = orderDAO;
    }

    @Override
    public void run() {
        try {
            Path reportFile = writeReport();
            try (BufferedReader reader = Files.newBufferedReader(reportFile, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOGGER.info(line);
                }
            }
            LOGGER.info("Daily sales report saved to " + reportFile);
        } catch (SQLException | IOException exception) {
            LOGGER.log(Level.SEVERE, "Could not generate the daily sales report.", exception);
        }
    }

    private Path writeReport() throws SQLException, IOException {
        LocalDate today = LocalDate.now();
        List<CustomerOrder> completed = orderDAO.findAll().stream()
                .filter(order -> order.getCreatedAt().toLocalDate().equals(today))
                .filter(order -> order.getStatus().equals("COMPLETED"))
                .toList();
        Path reportFile = ReportFiles.dailySalesReport();
        Files.createDirectories(reportFile.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8)) {
            writer.write("Daily sales report for " + today);
            writer.newLine();
            writer.write("Completed orders: " + completed.size());
            writer.newLine();
            writer.write("Order ID | Customer ID | Total");
            writer.newLine();
            for (CustomerOrder order : completed) {
                writer.write(order.getId() + " | " + order.getCustomerId()
                        + " | INR " + order.getTotalAmount());
                writer.newLine();
            }
        }
        return reportFile;
    }
}
