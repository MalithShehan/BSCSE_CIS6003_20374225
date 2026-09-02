package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ApiResponse;
import model.ReportItem;
import service.ReportService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Decision-Support Executive Reporting.
 * Route: GET /api/reports
 */
@WebServlet(name = "ReportServlet", urlPatterns = {"/api/reports"})
public class ReportServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ReportServlet.class.getName());
    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String type = request.getParameter("type");
            if (type == null || type.trim().isEmpty() || type.equalsIgnoreCase("summary")) {
                Map<String, Object> summary = reportService.getDashboardSummary();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Dashboard analytics summary retrieved.", summary)));
                return;
            }

            switch (type.trim().toLowerCase()) {
                case "daily":
                    List<ReportItem> dailyReport = reportService.getDailyReport();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(JsonUtil.toJson(ApiResponse.ok("Daily appointment report retrieved.", dailyReport)));
                    break;

                case "monthly":
                    List<ReportItem> monthlyReport = reportService.getMonthlyRevenueReport();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(JsonUtil.toJson(ApiResponse.ok("Monthly revenue report retrieved.", monthlyReport)));
                    break;

                case "dentist":
                    List<ReportItem> dentistReport = reportService.getDentistPerformanceReport();
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.print(JsonUtil.toJson(ApiResponse.ok("Dentist performance report retrieved.", dentistReport)));
                    break;

                default:
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(JsonUtil.toJson(ApiResponse.error("Invalid report type. Supported types: daily, monthly, dentist, summary.")));
                    break;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in ReportServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to query report views from database.")));
        }
    }
}
