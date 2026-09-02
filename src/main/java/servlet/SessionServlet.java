package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ApiResponse;
import model.User;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * REST Endpoint for inspecting the currently active authentication session.
 * Route: GET /api/session
 */
@WebServlet(name = "SessionServlet", urlPatterns = {"/api/session"})
public class SessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            
            // Clean user representation
            User sanitizedUser = new User();
            sanitizedUser.setUserId(user.getUserId());
            sanitizedUser.setUsername(user.getUsername());
            sanitizedUser.setFullName(user.getFullName());
            sanitizedUser.setRole(user.getRole());
            sanitizedUser.setActive(user.isActive());

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Active session verified.", sanitizedUser)));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(JsonUtil.toJson(ApiResponse.error("No active session found. Please log in.")));
        }
    }
}
