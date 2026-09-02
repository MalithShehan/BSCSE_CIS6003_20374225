package servlet;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ApiResponse;
import model.Appointment;
import service.AppointmentService;
import util.JsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Endpoint for Appointment Registration, Searching, Filtering, and Lifecycle Management.
 * Route: /api/appointments
 */
@WebServlet(name = "AppointmentServlet", urlPatterns = {"/api/appointments"})
public class AppointmentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AppointmentServlet.class.getName());
    private final AppointmentService appointmentService = new AppointmentService();

    /**
     * GET /api/appointments
     * Optional Query Parameters:
     * - appointmentNumber: SDC-2026-0001 (Search by unique reference)
     * - date: YYYY-MM-DD
     * - dentistId: Integer
     * - status: SCHEDULED | COMPLETED | CANCELLED | ALL
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String appointmentNumber = request.getParameter("appointmentNumber");
            if (appointmentNumber != null && !appointmentNumber.trim().isEmpty()) {
                Appointment appointment = appointmentService.getAppointmentByNumber(appointmentNumber);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Appointment details found.", appointment)));
                return;
            }

            String date = request.getParameter("date");
            String dentistIdStr = request.getParameter("dentistId");
            Integer dentistId = (dentistIdStr != null && !dentistIdStr.trim().isEmpty()) ? Integer.parseInt(dentistIdStr.trim()) : null;
            String status = request.getParameter("status");

            List<Appointment> list = appointmentService.getAppointments(date, dentistId, status);
            response.setStatus(HttpServletResponse.SC_OK);
            out.print(JsonUtil.toJson(ApiResponse.ok("Appointments retrieved successfully.", list)));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in GET AppointmentServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to query appointment database.")));
        }
    }

    /**
     * POST /api/appointments
     * Body JSON:
     * {
     *   "patientName": "...",
     *   "patientAddress": "...",
     *   "patientContact": "0771234567",
     *   "patientEmail": "...",
     *   "dentistId": 1,
     *   "treatmentId": 2,
     *   "appointmentDate": "2026-09-05",
     *   "appointmentTime": "09:30:00",
     *   "notes": "..."
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JsonObject json = JsonUtil.fromJson(request, JsonObject.class);
            if (json == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("Request payload cannot be empty.")));
                return;
            }

            String patientName = json.has("patientName") ? json.get("patientName").getAsString() : null;
            String patientAddress = json.has("patientAddress") ? json.get("patientAddress").getAsString() : null;
            String patientContact = json.has("patientContact") ? json.get("patientContact").getAsString() : null;
            String patientEmail = json.has("patientEmail") ? json.get("patientEmail").getAsString() : null;

            int dentistId = json.has("dentistId") ? json.get("dentistId").getAsInt() : 0;
            int treatmentId = json.has("treatmentId") ? json.get("treatmentId").getAsInt() : 0;

            String dateStr = json.has("appointmentDate") ? json.get("appointmentDate").getAsString() : null;
            String timeStr = json.has("appointmentTime") ? json.get("appointmentTime").getAsString() : null;
            String notes = json.has("notes") ? json.get("notes").getAsString() : null;

            if (dateStr == null || timeStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("Appointment date and time are required.")));
                return;
            }

            Date appDate = Date.valueOf(dateStr.trim());
            if (timeStr.length() == 5) {
                timeStr += ":00";
            }
            Time appTime = Time.valueOf(timeStr.trim());

            Appointment createdAppointment = appointmentService.bookAppointment(
                    patientName, patientAddress, patientContact, patientEmail,
                    dentistId, treatmentId, appDate, appTime, notes
            );

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(JsonUtil.toJson(ApiResponse.ok("Appointment successfully registered with Reference Number: " 
                    + createdAppointment.getAppointmentNumber(), createdAppointment)));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (IllegalStateException e) {
            // Conflict (e.g. Double booking)
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in POST AppointmentServlet", e);
            String message = "Database error occurred during appointment booking.";
            if (e.getMessage() != null && e.getMessage().contains("Conflict")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                message = e.getMessage();
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            out.print(JsonUtil.toJson(ApiResponse.error(message)));
        }
    }

    /**
     * PUT /api/appointments
     * Body JSON:
     * {
     *   "appointmentId": 1,
     *   "status": "COMPLETED" | "CANCELLED"
     * }
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            JsonObject json = JsonUtil.fromJson(request, JsonObject.class);
            if (json == null || !json.has("appointmentId") || !json.has("status")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("appointmentId and status are required.")));
                return;
            }

            int appointmentId = json.get("appointmentId").getAsInt();
            String status = json.get("status").getAsString();

            boolean updated = appointmentService.updateAppointmentStatus(appointmentId, status);
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Appointment status updated to " + status.toUpperCase(), null)));
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("Failed to update appointment status.")));
            }

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in PUT AppointmentServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to update appointment.")));
        }
    }

    /**
     * DELETE /api/appointments?id=1
     * Cancels an appointment.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(JsonUtil.toJson(ApiResponse.error("Appointment ID is required for cancellation.")));
                return;
            }

            int appointmentId = Integer.parseInt(idStr.trim());
            boolean cancelled = appointmentService.updateAppointmentStatus(appointmentId, "CANCELLED");

            if (cancelled) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(JsonUtil.toJson(ApiResponse.ok("Appointment successfully cancelled.", null)));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.toJson(ApiResponse.error("Appointment not found with ID: " + appointmentId)));
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error("Invalid appointment ID format.")));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(JsonUtil.toJson(ApiResponse.error(e.getMessage())));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error in DELETE AppointmentServlet", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.toJson(ApiResponse.error("Failed to cancel appointment.")));
        }
    }
}
