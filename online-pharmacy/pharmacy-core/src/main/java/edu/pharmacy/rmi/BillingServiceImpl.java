package edu.pharmacy.rmi;

import edu.pharmacy.dao.OrderDAO;
import edu.pharmacy.model.CustomerOrder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Locale;
import java.util.Optional;

/** RMI implementation that exposes pricing and billing calculations for the desktop client. */
public class BillingServiceImpl extends UnicastRemoteObject implements BillingService {
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    private final OrderDAO orderDAO = new OrderDAO();

    public BillingServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public BigDecimal calculateBill(long orderId) throws RemoteException {
        try {
            Optional<CustomerOrder> order = orderDAO.findById(orderId);
            if (order.isEmpty()) {
                return BigDecimal.ZERO;
            }
            return order.get().getTotalAmount().setScale(2, RoundingMode.HALF_UP);
        } catch (Exception exception) {
            throw new RemoteException("Unable to read order " + orderId + ".", exception);
        }
    }

    @Override
    public BigDecimal applyDiscount(String code, BigDecimal amount) throws RemoteException {
        if (amount == null || amount.signum() < 0) {
            throw new RemoteException("Discount amount must be greater than or equal to zero.");
        }

        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        BigDecimal discountRate = switch (normalized) {
            case "WELCOME10" -> new BigDecimal("0.10");
            case "VIP20" -> new BigDecimal("0.20");
            case "NEWUSER15" -> new BigDecimal("0.15");
            default -> BigDecimal.ZERO;
        };
        return amount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateGST(BigDecimal amount) throws RemoteException {
        if (amount == null || amount.signum() < 0) {
            throw new RemoteException("GST amount must be greater than or equal to zero.");
        }
        return amount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
