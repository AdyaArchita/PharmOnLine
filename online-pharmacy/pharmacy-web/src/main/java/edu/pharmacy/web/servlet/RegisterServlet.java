package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.UserDAO;
import edu.pharmacy.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = value(request, "fullName");
        String email = value(request, "email");
        String address = value(request, "address");
        String password = request.getParameter("password");
        String confirmation = request.getParameter("confirmPassword");

        if (fullName.isBlank() || !EMAIL_PATTERN.matcher(email).matches()
                || password == null || !password.equals(confirmation)) {
            request.setAttribute("error", "Enter a name and valid email, and make sure "
                    + "both password fields match.");
            doGet(request, response);
            return;
        }

        char[] passwordChars = password.toCharArray();
        try {
            Customer customer = new Customer(0, fullName, email, address, null, null);
            userDAO.create(customer, passwordChars);
            response.sendRedirect(request.getContextPath() + "/login?registered=1");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
            doGet(request, response);
        } catch (SQLException exception) {
            if (exception.getSQLState() != null && exception.getSQLState().startsWith("23")) {
                request.setAttribute("error", "That email address is already registered.");
                doGet(request, response);
            } else {
                getServletContext().log("Customer registration failed.", exception);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("error", "Registration is temporarily unavailable.");
                doGet(request, response);
            }
        } catch (GeneralSecurityException exception) {
            getServletContext().log("Password hashing failed during registration.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "Registration is temporarily unavailable.");
            doGet(request, response);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    private static String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }
}
