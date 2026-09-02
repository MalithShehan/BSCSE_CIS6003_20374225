package servlet;

import dao.DentistDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ApiResponse;
import model.Dentist;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Dentist Practitioner catalog.
 * Route: GET /api/dentists
 */
@WebServlet(name = "DentistServlet", urlPatterns = {"/api/dentists"})
public class DentistServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DentistServlet.class.getName());
    private final DentistDAO dentistDAO = new DentistDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                int id = Integer.parseInt(idParam.trim());
                Dentist dentist = dentistDAO.findById(id);
                if (dentist != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(JsonUtil.toJson(ApiResponse.ok("Dentist details retrieved.", dentist)));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.toJson(ApiResponse.error("Dentist not found with ID: " + id)));
                }
                return;
            }

            List<Dentist> dentists = dentistDAO.findAllActive();
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Active dentists retrieved successfully.", dentists)));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error("Invalid dentist ID parameter format.")));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in DentistServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to retrieve dentists from database.")));
        }
    }
}
