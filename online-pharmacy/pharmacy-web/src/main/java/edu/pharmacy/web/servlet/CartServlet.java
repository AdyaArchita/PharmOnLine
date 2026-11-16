package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.dao.PrescriptionDAO;
import edu.pharmacy.model.Medicine;
import edu.pharmacy.web.CartViewSupport;
import edu.pharmacy.web.bean.CartBean;
import edu.pharmacy.web.bean.UserBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        CartBean cart = cart(session);
        UserBean user = (UserBean) session.getAttribute("user");
        try {
            request.setAttribute("cartItems",
                    CartViewSupport.load(cart, user.getId(), medicineDAO, prescriptionDAO));
            request.setAttribute("flashError", session.getAttribute("flashError"));
            session.removeAttribute("flashError");
            request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not load cart details.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "Cart details are temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        CartBean cart = cart(session);
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                long medicineId = positiveLong(request.getParameter("medicineId"));
                int quantity = positiveInt(request.getParameter("quantity"));
                Medicine medicine = medicineDAO.findById(medicineId)
                        .orElseThrow(() -> new IllegalArgumentException("Medicine was not found."));
                int currentQuantity = cart.getItems().stream()
                        .filter(item -> item.getMedicineId() == medicineId)
                        .mapToInt(item -> item.getQuantity()).findFirst().orElse(0);
                validateCartQuantity(medicine, Math.addExact(currentQuantity, quantity));
                cart.addMedicine(medicineId, quantity);
            } else if ("update".equals(action)) {
                long medicineId = positiveLong(request.getParameter("medicineId"));
                int quantity = positiveInt(request.getParameter("quantity"));
                Medicine medicine = medicineDAO.findById(medicineId)
                        .orElseThrow(() -> new IllegalArgumentException("Medicine was not found."));
                validateCartQuantity(medicine, quantity);
                cart.updateQuantity(medicineId, quantity);
            } else if ("remove".equals(action)) {
                cart.removeMedicine(positiveLong(request.getParameter("medicineId")));
            } else if ("clear".equals(action)) {
                cart.clear();
            } else {
                throw new IllegalArgumentException("Unknown cart action.");
            }
        } catch (IllegalArgumentException | ArithmeticException exception) {
            session.setAttribute("flashError", exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Could not update cart.", exception);
            session.setAttribute("flashError", "The cart could not be updated right now.");
        }
        response.sendRedirect(request.getContextPath() + "/cart");
    }

    private static void validateCartQuantity(Medicine medicine, int quantity) {
        if (medicine.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("That medicine has expired and cannot be sold.");
        }
        if (quantity > medicine.getStock()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock.");
        }
    }

    private static CartBean cart(HttpSession session) {
        CartBean cart = (CartBean) session.getAttribute("cart");
        if (cart == null) {
            cart = new CartBean();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    private static long positiveLong(String value) {
        long parsed = Long.parseLong(value);
        if (parsed <= 0) {
            throw new IllegalArgumentException("Id must be greater than zero.");
        }
        return parsed;
    }

    private static int positiveInt(String value) {
        int parsed = Integer.parseInt(value);
        if (parsed <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        return parsed;
    }
}
