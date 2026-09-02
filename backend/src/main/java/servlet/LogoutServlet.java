package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.ApiResponse;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * REST Endpoint for logging out and invalidating HTTP session.
 * Route: POST /api/logout
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/api/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(JsonUtil.toJson(ApiResponse.ok("Logged out successfully.", null)));
    }
}
