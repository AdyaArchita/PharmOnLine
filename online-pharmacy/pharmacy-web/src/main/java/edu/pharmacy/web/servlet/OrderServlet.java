package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.OrderDAO;
import edu.pharmacy.dao.PrescriptionDAO;
import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.exception.ExpiredMedicineException;
import edu.pharmacy.exception.InvalidPrescriptionException;
import edu.pharmacy.exception.OutOfStockException;
import edu.pharmacy.model.OrderItem;
import edu.pharmacy.web.CartViewSupport;
import edu.pharmacy.web.bean.CartBean;
import edu.pharmacy.web.bean.OrderBean;
import edu.pharmacy.web.bean.UserBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;

@WebServlet(urlPatterns = {"/checkout", "/orders"})
public class OrderServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getServletPath().equals("/orders")) {
            showOrders(request, response);
        } else {
            showCheckout(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        CartBean cart = (CartBean) session.getAttribute("cart");
        UserBean user = (UserBean) session.getAttribute("user");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        List<OrderItem> items = cart.getItems().stream()
                .map(item -> new OrderItem(item.getMedicineId(), item.getQuantity(),
                        item.getPrescriptionId()))
                .toList();
        try {
            orderDAO.placeOrder(user.getId(), items);
            cart.clear();
            response.sendRedirect(request.getContextPath() + "/orders");
        } catch (OutOfStockException | ExpiredMedicineException
                 | InvalidPrescriptionException exception) {
            request.setAttribute("error", exception.getMessage());
            showCheckout(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not place customer order.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "The order could not be placed right now.");
            showCheckout(request, response);
        }
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        CartBean cart = (CartBean) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        UserBean user = (UserBean) session.getAttribute("user");
        try {
            request.setAttribute("cartItems",
                    CartViewSupport.load(cart, user.getId(), medicineDAO, prescriptionDAO));
            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not load checkout details.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "Checkout is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void showOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserBean user = (UserBean) request.getSession().getAttribute("user");
        try {
            List<OrderBean> orders = orderDAO.findByCustomerId(user.getId()).stream()
                    .map(order -> {
                        OrderBean bean = new OrderBean();
                        bean.setId(order.getId());
                        bean.setCreatedAt(java.util.Date.from(order.getCreatedAt()
                                .atZone(ZoneId.systemDefault()).toInstant()));
                        bean.setStatus(order.getStatus());
                        bean.setTotalAmount(order.getTotalAmount());
                        return bean;
                    }).toList();
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not load order history.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "Order history is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }
}
