package edu.pharmacy.demo;

import edu.pharmacy.exception.ExpiredMedicineException;
import edu.pharmacy.exception.InsufficientBalanceException;
import edu.pharmacy.exception.InvalidPrescriptionException;
import edu.pharmacy.exception.OutOfStockException;
import edu.pharmacy.model.Admin;
import edu.pharmacy.model.Customer;
import edu.pharmacy.model.OTCMedicine;
import edu.pharmacy.model.Pharmacist;
import edu.pharmacy.model.PrescriptionMedicine;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Demonstrates key OOP and exception-handling techniques required in the lab. */
public final class OopDemo {
    private OopDemo() {
    }

    public static void runDemo() {
        Customer customer = new Customer(1L, "Asha Verma", "asha@example.com",
                "12 Lake Road", "hash-1", "salt-1");
        Pharmacist pharmacist = new Pharmacist(2L, "Dr. Mehul Shah", "mehul@example.com",
                "PH-2024", "hash-2", "salt-2");
        Admin admin = new Admin(3L, "Ria Nair", "ria@example.com", "Operations",
                "hash-3", "salt-3");

        OTCMedicine antacid = new OTCMedicine(10L, "Antacid", "Fast relief for acidity",
                1L, 5L, new BigDecimal("45.00"), 10, LocalDate.now().plusMonths(6));
        PrescriptionMedicine antibiotic = new PrescriptionMedicine(11L, "Amoxicillin",
                "Antibiotic capsule", 2L, 6L, new BigDecimal("120.00"), 4,
                LocalDate.now().plusDays(30));

        System.out.println("Person inheritance demo:");
        System.out.println(customer.getFullName() + " -> " + customer.getRole());
        System.out.println(pharmacist.getFullName() + " -> " + pharmacist.getRole());
        System.out.println(admin.getFullName() + " -> " + admin.getRole());

        try {
            antacid.validateForSale(12, false);
        } catch (OutOfStockException | ExpiredMedicineException | InvalidPrescriptionException exception) {
            System.out.println("OTC stock validation: " + exception.getMessage());
        }

        try {
            antibiotic.validateForSale(1, false);
        } catch (InvalidPrescriptionException exception) {
            System.out.println("Prescription validation: " + exception.getMessage());
        } catch (OutOfStockException | ExpiredMedicineException exception) {
            System.out.println("Unexpected medicine issue: " + exception.getMessage());
        } finally {
            System.out.println("Prescription check finally block executed.");
        }

        try {
            throw new ExpiredMedicineException("A medicine has expired and needs clearance.");
        } catch (ExpiredMedicineException exception) {
            System.out.println("Expired medicine caught: " + exception.getMessage());
        } finally {
            System.out.println("Expired medicine finally block executed.");
        }

        try {
            throw new InsufficientBalanceException("Customer payment balance is not enough.");
        } catch (InsufficientBalanceException exception) {
            System.out.println("Balance check caught: " + exception.getMessage());
        }

        System.out.println("Billable demo: " + antacid.calculateTotal(2));
        System.out.println("Medicine search demo: " + antacid.matches("acid"));
        System.out.println("Reportable demo: " + antacid.toReportLine());
    }

    public static void main(String[] args) {
        runDemo();
    }
}
