package edu.pharmacy.web.servlet;

import edu.pharmacy.dao.UserDAO;
import edu.pharmacy.model.Customer;
import edu.pharmacy.model.Person;
import edu.pharmacy.web.bean.UserBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || password == null || email.isBlank()
                || password.length() < 8 || password.length() > 128) {
            request.setAttribute("error", "Enter your email and password (at least 8 characters).");
            doGet(request, response);
            return;
        }

        char[] passwordChars = password.toCharArray();
        try {
            var result = userDAO.authenticate(email, passwordChars);
            if (result.isEmpty() || !(result.get() instanceof Customer)) {
                request.setAttribute("error", "The email or password was not accepted.");
                doGet(request, response);
                return;
            }

            Person person = result.get();
            HttpSession previousSession = request.getSession(false);
            if (previousSession != null) {
                previousSession.invalidate();
            }
            UserBean user = new UserBean();
            user.setId(person.getId());
            user.setFullName(person.getFullName());
            user.setEmail(person.getEmail());
            user.setRole(person.getRole().name());
            request.getSession(true).setAttribute("user", user);
            response.sendRedirect(request.getContextPath() + "/medicines");
        } catch (SQLException | GeneralSecurityException exception) {
            getServletContext().log("Customer login failed.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("error", "Login is temporarily unavailable.");
            doGet(request, response);
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }
}
