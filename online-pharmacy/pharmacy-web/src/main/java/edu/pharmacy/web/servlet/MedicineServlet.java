package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.MedicineDAO;
import edu.pharmacy.model.Medicine;
import edu.pharmacy.web.bean.MedicineBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/medicines")
public class MedicineServlet extends HttpServlet {
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        try {
            List<Medicine> medicines = query == null || query.isBlank()
                    ? medicineDAO.findAll() : medicineDAO.search(query);
            request.setAttribute("medicines", medicines.stream()
                    .map(MedicineServlet::toBean).toList());
            request.setAttribute("query", query == null ? "" : query);
            request.getRequestDispatcher("/WEB-INF/views/medicines.jsp")
                    .forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Could not load medicine catalog.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "The medicine catalog is temporarily unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private static MedicineBean toBean(Medicine medicine) {
        MedicineBean bean = new MedicineBean();
        bean.setId(medicine.getId());
        bean.setName(medicine.getName());
        bean.setDescription(medicine.getDescription());
        bean.setCategoryId(medicine.getCategoryId());
        bean.setSupplierId(medicine.getSupplierId());
        bean.setUnitPrice(medicine.getUnitPrice());
        bean.setStock(medicine.getStock());
        bean.setExpiryDate(medicine.getExpiryDate());
        bean.setPrescriptionRequired(medicine.isPrescriptionRequired());
        return bean;
    }
}
