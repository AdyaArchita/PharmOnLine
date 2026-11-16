package edu.pharmacy.rmi;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

/** Remote billing API for the pharmacy order settlement workflow. */
public interface BillingService extends Remote {
    BigDecimal calculateBill(long orderId) throws RemoteException;

    BigDecimal applyDiscount(String code, BigDecimal amount) throws RemoteException;

    BigDecimal calculateGST(BigDecimal amount) throws RemoteException;
}
