package servlet;

import dao.TreatmentDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ApiResponse;
import model.Treatment;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Treatment Services Catalog.
 * Route: GET /api/treatments
 */
@WebServlet(name = "TreatmentServlet", urlPatterns = {"/api/treatments"})
public class TreatmentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TreatmentServlet.class.getName());
    private final TreatmentDAO treatmentDAO = new TreatmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String idParam = request.getParameter("id");
            if (idParam != null && !idParam.trim().isEmpty()) {
                int id = Integer.parseInt(idParam.trim());
                Treatment treatment = treatmentDAO.findById(id);
                if (treatment != null) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(JsonUtil.toJson(ApiResponse.ok("Treatment details retrieved.", treatment)));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(JsonUtil.toJson(ApiResponse.error("Treatment not found with ID: " + id)));
                }
                return;
            }

            List<Treatment> treatments = treatmentDAO.findAllActive();
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Active treatments retrieved successfully.", treatments)));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error("Invalid treatment ID parameter format.")));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in TreatmentServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to retrieve treatment catalog.")));
        }
    }
}
