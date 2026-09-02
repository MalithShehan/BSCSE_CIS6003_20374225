package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ApiResponse;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Authentication and Authorization Security Filter.
 * Protects all /api/* REST endpoints except whitelist (e.g., /api/login).
 * Enforces CORS headers and active session validation.
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/api/*"})
public class AuthFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());

    // Public endpoints that do not require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/login"
    );

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info("AuthFilter initialized successfully.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI().substring(request.getContextPath().length());

        // 1. Add CORS and Security Headers
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin") != null ? request.getHeader("Origin") : "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Allow pre-flight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2. Allow Public Endpoints
        for (String publicPath : PUBLIC_ENDPOINTS) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                chain.doFilter(req, res);
                return;
            }
        }

        // 3. Validate Active Session
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("user") != null);

        if (isAuthenticated) {
            chain.doFilter(req, res);
        } else {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter out = response.getWriter();
            out.print(JsonUtil.toJson(ApiResponse.error("Unauthorized: Please log in to access this resource.")));
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthFilter destroyed.");
    }
}
