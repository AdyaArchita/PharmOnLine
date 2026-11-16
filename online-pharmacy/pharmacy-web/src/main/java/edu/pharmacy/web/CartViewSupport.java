package edu.pharmacy.web;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.dao.PrescriptionDAO;
import edu.pharmacy.model.Medicine;
import edu.pharmacy.web.bean.CartBean;
import edu.pharmacy.web.bean.CartItemBean;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Refreshes session-cart descriptions and prices from the shared database. */
public final class CartViewSupport {
    private CartViewSupport() {
    }

    public static List<CartItemBean> load(CartBean cart, long customerId,
                                          MedicineDAO medicineDAO,
                                          PrescriptionDAO prescriptionDAO)
            throws SQLException {
        List<CartItemBean> rows = new ArrayList<>();
        for (CartItemBean item : cart.getItems()) {
            var medicine = medicineDAO.findById(item.getMedicineId());
            if (medicine.isEmpty()) {
                cart.removeMedicine(item.getMedicineId());
                continue;
            }
            Medicine current = medicine.get();
            item.setName(current.getName());
            item.setUnitPrice(current.getUnitPrice());
            item.setAvailableStock(current.getStock());
            item.setExpiryDate(current.getExpiryDate());
            item.setPrescriptionRequired(current.isPrescriptionRequired());
            item.setPrescriptionStatus(null);
            if (item.getPrescriptionId() != null) {
                item.setPrescriptionStatus(prescriptionDAO
                        .findStatus(customerId, item.getPrescriptionId()).orElse("NOT FOUND"));
            }
            rows.add(item);
        }
        return rows;
    }
}
