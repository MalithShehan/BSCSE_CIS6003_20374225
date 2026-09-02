package servlet;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ApiResponse;
import model.User;
import service.AuthService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Staff User Authentication.
 * Route: POST /api/login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JsonObject jsonRequest = JsonUtil.fromJson(request, JsonObject.class);
            if (jsonRequest == null || !jsonRequest.has("username") || !jsonRequest.has("password")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("Username and password are required.")));
                return;
            }

            String username = jsonRequest.get("username").getAsString();
            String password = jsonRequest.get("password").getAsString();

            User user = authService.authenticate(username, password);

            // Create or invalidate existing session
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("username", user.getUsername());
            session.setMaxInactiveInterval(3600); // 1 hour session expiry

            // Sanitized user object without password hash for JSON response
            User sanitizedUser = new User();
            sanitizedUser.setUserId(user.getUserId());
            sanitizedUser.setUsername(user.getUsername());
            sanitizedUser.setFullName(user.getFullName());
            sanitizedUser.setRole(user.getRole());
            sanitizedUser.setActive(user.isActive());

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Login successful. Welcome back, " + user.getFullName() + "!", sanitizedUser)));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SecurityException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("A database error occurred during login. Please try again later.")));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in LoginServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Invalid request payload or server error.")));
        }
    }
}
