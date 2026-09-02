package dao;

import config.DatabaseConnection;
import model.Appointment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Appointment scheduling, search, status management, and reporting joins.
 */
public class AppointmentDAO {

    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    /**
     * Creates and persists a new appointment record.
     */
    public int createAppointment(Appointment app) throws SQLException {
        String sql = "INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, " +
                     "appointment_date, appointment_time, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, app.getAppointmentNumber());
            stmt.setInt(2, app.getPatientId());
            stmt.setInt(3, app.getDentistId());
            stmt.setInt(4, app.getTreatmentId());
            stmt.setDate(5, app.getAppointmentDate());
            stmt.setTime(6, app.getAppointmentTime());
            stmt.setString(7, app.getStatus() != null ? app.getStatus() : "SCHEDULED");
            stmt.setString(8, app.getNotes());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating appointment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    app.setAppointmentId(generatedKeys.getInt(1));
                    return app.getAppointmentId();
                } else {
                    throw new SQLException("Creating appointment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating appointment: " + app.getAppointmentNumber(), e);
            throw e;
        }
    }

    /**
     * Searches appointment by unique reference number (e.g., SDC-2026-0001)
     * Returns full rich details including patient, dentist, treatment names and bill totals.
     */
    public Appointment findByAppointmentNumber(String appointmentNumber) throws SQLException {
        String sql = buildJoinedSelectQuery() + " WHERE a.appointment_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointmentNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToAppointment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointment by number: " + appointmentNumber, e);
            throw e;
        }
        return null;
    }

    /**
     * Finds appointment by primary key ID.
     */
    public Appointment findById(int appointmentId) throws SQLException {
        String sql = buildJoinedSelectQuery() + " WHERE a.appointment_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToAppointment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointment by ID: " + appointmentId, e);
            throw e;
        }
        return null;
    }

    /**
     * Retrieves all appointments with optional query filters (date, dentistId, status).
     */
    public List<Appointment> findAllFiltered(String dateStr, Integer dentistId, String status) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(buildJoinedSelectQuery());
        sql.append(" WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append(" AND a.appointment_date = ? ");
            params.add(Date.valueOf(dateStr.trim()));
        }
        if (dentistId != null && dentistId > 0) {
            sql.append(" AND a.dentist_id = ? ");
            params.add(dentistId);
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append(" AND a.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append(" ORDER BY a.appointment_date DESC, a.appointment_time ASC");

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoinedResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving filtered appointments", e);
            throw e;
        }
        return list;
    }

    /**
     * Updates the status of an appointment (e.g., SCHEDULED -> COMPLETED / CANCELLED).
     */
    public boolean updateStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.toUpperCase());
            stmt.setInt(2, appointmentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating appointment status for ID: " + appointmentId, e);
            throw e;
        }
    }

    /**
     * Checks if a dentist already has a conflicting active appointment.
     */
    public boolean isDoubleBooked(int dentistId, Date date, Time time, Integer excludeAppointmentId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? " +
                                              "AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED'");
        if (excludeAppointmentId != null) {
            sql.append(" AND appointment_id != ?");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setInt(1, dentistId);
            stmt.setDate(2, date);
            stmt.setTime(3, time);
            if (excludeAppointmentId != null) {
                stmt.setInt(4, excludeAppointmentId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking double-booking conflict", e);
            throw e;
        }
        return false;
    }

    /**
     * Retrieves the next sequence number for the given year to construct SDC-YYYY-XXXX.
     */
    public int getNextSequenceNumber(int year) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_number LIKE ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "SDC-" + year + "-%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching next sequence number for year " + year, e);
            throw e;
        }
        return 1;
    }

    /**
     * Base SQL select clause joining appointments with patients, dentists, treatments, and invoices.
     */
    private String buildJoinedSelectQuery() {
        return "SELECT a.appointment_id, a.appointment_number, a.patient_id, a.dentist_id, a.treatment_id, " +
               "a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, a.updated_at, " +
               "p.patient_name, p.address AS patient_address, p.contact_number AS patient_contact, p.email AS patient_email, " +
               "d.dentist_name, d.specialization AS dentist_specialization, d.consultation_fee, " +
               "t.treatment_name, t.cost AS treatment_cost, " +
               "(d.consultation_fee + t.cost) AS total_estimated_cost, " +
               "(CASE WHEN i.invoice_id IS NOT NULL THEN TRUE ELSE FALSE END) AS is_billed " +
               "FROM appointments a " +
               "JOIN patients p ON a.patient_id = p.patient_id " +
               "JOIN dentists d ON a.dentist_id = d.dentist_id " +
               "JOIN treatments t ON a.treatment_id = t.treatment_id " +
               "LEFT JOIN invoices i ON a.appointment_id = i.appointment_id";
    }

    private Appointment mapJoinedResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment app = new Appointment();
        app.setAppointmentId(rs.getInt("appointment_id"));
        app.setAppointmentNumber(rs.getString("appointment_number"));
        app.setPatientId(rs.getInt("patient_id"));
        app.setDentistId(rs.getInt("dentist_id"));
        app.setTreatmentId(rs.getInt("treatment_id"));
        app.setAppointmentDate(rs.getDate("appointment_date"));
        app.setAppointmentTime(rs.getTime("appointment_time"));
        app.setStatus(rs.getString("status"));
        app.setNotes(rs.getString("notes"));
        app.setCreatedAt(rs.getTimestamp("created_at"));
        app.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Presentation / Joined details
        app.setPatientName(rs.getString("patient_name"));
        app.setPatientAddress(rs.getString("patient_address"));
        app.setPatientContact(rs.getString("patient_contact"));
        app.setPatientEmail(rs.getString("patient_email"));
        app.setDentistName(rs.getString("dentist_name"));
        app.setDentistSpecialization(rs.getString("dentist_specialization"));
        app.setDentistFee(rs.getBigDecimal("consultation_fee"));
        app.setTreatmentName(rs.getString("treatment_name"));
        app.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        app.setTotalEstimatedCost(rs.getBigDecimal("total_estimated_cost"));
        app.setBilled(rs.getBoolean("is_billed"));

        return app;
    }
}
