package edu.pharmacy.web.bean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JavaBean session cart; prices are refreshed from the database before display. */
public class CartBean {
    private final Map<Long, CartItemBean> items = new LinkedHashMap<>();

    public CartBean() {
    }

    public void addMedicine(long medicineId, int quantity) {
        if (medicineId <= 0 || quantity <= 0) {
            throw new IllegalArgumentException("Medicine and quantity must be positive.");
        }
        CartItemBean item = items.get(medicineId);
        if (item == null) {
            item = new CartItemBean();
            item.setMedicineId(medicineId);
            item.setQuantity(quantity);
            items.put(medicineId, item);
        } else {
            item.setQuantity(Math.addExact(item.getQuantity(), quantity));
        }
    }

    public void updateQuantity(long medicineId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        CartItemBean item = items.get(medicineId);
        if (item == null) {
            throw new IllegalArgumentException("That medicine is not in the cart.");
        }
        item.setQuantity(quantity);
    }

    public void setPrescriptionId(long medicineId, long prescriptionId) {
        CartItemBean item = items.get(medicineId);
        if (item == null || prescriptionId <= 0) {
            throw new IllegalArgumentException("Upload a prescription for a cart item.");
        }
        item.setPrescriptionId(prescriptionId);
    }

    public void removeMedicine(long medicineId) {
        items.remove(medicineId);
    }

    public void clear() {
        items.clear();
    }

    public List<CartItemBean> getItems() {
        return new ArrayList<>(items.values());
    }

    public void setItems(List<CartItemBean> newItems) {
        items.clear();
        if (newItems != null) {
            for (CartItemBean item : newItems) {
                items.put(item.getMedicineId(), item);
            }
        }
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItemBean::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
