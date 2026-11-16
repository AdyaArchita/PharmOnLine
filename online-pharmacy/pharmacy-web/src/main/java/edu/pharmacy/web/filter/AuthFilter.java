package edu.pharmacy.web.filter;

import edu.pharmacy.model.Role;
import edu.pharmacy.web.bean.UserBean;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Restricts cart, checkout, order history, uploads, and logout to signed-in customers. */
@WebFilter(urlPatterns = {"/cart", "/checkout", "/orders", "/prescription", "/logout"})
public class AuthFilter implements Filter {
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        UserBean user = session == null ? null : (UserBean) session.getAttribute("user");
        if (user == null || !Role.CUSTOMER.name().equals(user.getRole())) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }
}
