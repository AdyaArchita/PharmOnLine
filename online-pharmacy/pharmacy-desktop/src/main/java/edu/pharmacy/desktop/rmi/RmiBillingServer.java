package edu.pharmacy.desktop.rmi;

import edu.pharmacy.rmi.BillingService;
import edu.pharmacy.rmi.BillingServiceImpl;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/** Starts the registry and binds the billing service for remote access. */
public final class RmiBillingServer {
    private static final String BINDING = "rmi://localhost:1099/pharmacy-billing";

    private RmiBillingServer() {
    }

    public static void main(String[] args) throws Exception {
        try {
            LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry started on port 1099.");
        } catch (Exception ignored) {
            System.out.println("Using an existing RMI registry on port 1099.");
        }

        BillingService service = new BillingServiceImpl();
        Naming.rebind(BINDING, service);
        System.out.println("Billing service bound at " + BINDING);
    }
}
