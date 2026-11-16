package edu.pharmacy.desktop.rmi;

import edu.pharmacy.rmi.BillingService;
import java.math.BigDecimal;
import java.rmi.Naming;

/** Demonstrates remote billing calculations from the desktop application. */
public final class RmiBillingClient {
    private static final String BINDING = "rmi://localhost:1099/pharmacy-billing";

    private RmiBillingClient() {
    }

    public static void main(String[] args) throws Exception {
        BillingService billingService = (BillingService) Naming.lookup(BINDING);

        long orderId = 1L;
        BigDecimal bill = billingService.calculateBill(orderId);
        BigDecimal gst = billingService.calculateGST(bill);
        BigDecimal discount = billingService.applyDiscount("WELCOME10", bill);

        System.out.println("Order " + orderId + " amount: INR " + bill.toPlainString());
        System.out.println("GST: INR " + gst.toPlainString());
        System.out.println("Discount: INR " + discount.toPlainString());
    }
}
