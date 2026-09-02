package servlet;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ApiResponse;
import model.Invoice;
import service.BillingService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Invoice Generation and Printable Receipt queries.
 * Route: /api/bill
 */
@WebServlet(name = "BillServlet", urlPatterns = {"/api/bill"})
public class BillServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BillServlet.class.getName());
    private final BillingService billingService = new BillingService();

    /**
     * GET /api/bill
     * Optional Query Params:
     * - appointmentNumber: SDC-2026-0001 (Fetch invoice for printing/receipt)
     * - id: Integer (Invoice ID)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String appNumber = request.getParameter("appointmentNumber");
            if (appNumber != null && !appNumber.trim().isEmpty()) {
                Invoice invoice = billingService.getInvoiceByAppointmentNumber(appNumber);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Invoice retrieved successfully.", invoice)));
                return;
            }

            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.trim().isEmpty()) {
                int invoiceId = Integer.parseInt(idStr.trim());
                Invoice invoice = billingService.getInvoiceById(invoiceId);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Invoice retrieved successfully.", invoice)));
                return;
            }

            List<Invoice> invoices = billingService.getAllInvoices();
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Invoices retrieved successfully.", invoices)));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error("Invalid invoice ID format.")));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in GET BillServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to query invoice records.")));
        }
    }

    /**
     * POST /api/bill
     * Body JSON:
     * {
     *   "appointmentId": 1,
     *   "discountPercentage": 10.0,
     *   "paymentMethod": "CASH"
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JsonObject json = JsonUtil.fromJson(request, JsonObject.class);
            if (json == null || !json.has("appointmentId")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("appointmentId is required to generate bill.")));
                return;
            }

            int appointmentId = json.get("appointmentId").getAsInt();
            BigDecimal discount = json.has("discountPercentage") ? 
                    json.get("discountPercentage").getAsBigDecimal() : BigDecimal.ZERO;
            String paymentMethod = json.has("paymentMethod") ? 
                    json.get("paymentMethod").getAsString() : "CASH";

            Invoice invoice = billingService.generateBill(appointmentId, discount, paymentMethod);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.toJson(ApiResponse.ok("Invoice generated successfully: " + invoice.getInvoiceNumber(), invoice)));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (IllegalStateException e) {
            // Duplicate billing or cancelled appointment
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in POST BillServlet", e);
            String message = "Database error occurred while generating invoice.";
            if (e.getMessage() != null && e.getMessage().contains("already been generated")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                message = e.getMessage();
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            out.print(JsonUtil.toJson(ApiResponse.error(message)));
        }
    }
}
