package dao;

import config.DatabaseConnection;
import model.Invoice;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Invoice generation and billing queries.
 * Integrates with MySQL Stored Procedure `GenerateBillForAppointment`.
 */
public class InvoiceDAO {

    private static final Logger LOGGER = Logger.getLogger(InvoiceDAO.class.getName());

    /**
     * Executes the MySQL Stored Procedure `GenerateBillForAppointment` to generate an official invoice.
     *
     * @param appointmentId       The target appointment ID
     * @param discountPercentage  Percentage discount (0.00 to 100.00)
     * @param paymentMethod       Payment mode (CASH, CREDIT_CARD, etc.)
     * @return Fully populated generated Invoice object
     * @throws SQLException on constraint violations, duplicate bills, or DB error
     */
    public Invoice generateBill(int appointmentId, BigDecimal discountPercentage, String paymentMethod) throws SQLException {
        String callSql = "{CALL GenerateBillForAppointment(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cstmt = conn.prepareCall(callSql)) {

            cstmt.setInt(1, appointmentId);
            cstmt.setBigDecimal(2, discountPercentage != null ? discountPercentage : BigDecimal.ZERO);
            cstmt.setString(3, paymentMethod != null ? paymentMethod : "CASH");

            // Register OUT parameters
            cstmt.registerOutParameter(4, Types.INTEGER);    // p_invoice_id
            cstmt.registerOutParameter(5, Types.VARCHAR);    // p_invoice_number
            cstmt.registerOutParameter(6, Types.DECIMAL);    // p_total_amount

            cstmt.execute();

            int generatedInvoiceId = cstmt.getInt(4);
            LOGGER.info("Generated invoice ID " + generatedInvoiceId + " via Stored Procedure");

            return findById(generatedInvoiceId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calling Stored Procedure GenerateBillForAppointment", e);
            throw e;
        }
    }

    /**
     * Finds an invoice by appointment reference number (e.g., SDC-2026-0001).
     */
    public Invoice findByAppointmentNumber(String appointmentNumber) throws SQLException {
        String sql = buildJoinedInvoiceQuery() + " WHERE a.appointment_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appointmentNumber.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by appointment number: " + appointmentNumber, e);
            throw e;
        }
        return null;
    }

    /**
     * Finds an invoice by its primary key ID.
     */
    public Invoice findById(int invoiceId) throws SQLException {
        String sql = buildJoinedInvoiceQuery() + " WHERE i.invoice_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, invoiceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by ID: " + invoiceId, e);
            throw e;
        }
        return null;
    }

    /**
     * Finds an invoice by its unique appointment ID.
     */
    public Invoice findByAppointmentId(int appointmentId) throws SQLException {
        String sql = buildJoinedInvoiceQuery() + " WHERE i.appointment_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapJoinedResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by appointment ID: " + appointmentId, e);
            throw e;
        }
        return null;
    }

    /**
     * Retrieves all invoices for financial auditing.
     */
    public List<Invoice> findAll() throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        String sql = buildJoinedInvoiceQuery() + " ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapJoinedResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all invoices", e);
            throw e;
        }
        return invoices;
    }

    private String buildJoinedInvoiceQuery() {
        return "SELECT i.invoice_id, i.invoice_number, i.appointment_id, i.consultation_fee, " +
               "i.treatment_cost, i.discount_percentage, i.discount_amount, i.total_amount, " +
               "i.payment_status, i.payment_method, i.created_at, " +
               "a.appointment_number, a.appointment_date, a.appointment_time, " +
               "p.patient_name, p.contact_number AS patient_contact, p.address AS patient_address, " +
               "d.dentist_name, t.treatment_name " +
               "FROM invoices i " +
               "JOIN appointments a ON i.appointment_id = a.appointment_id " +
               "JOIN patients p ON a.patient_id = p.patient_id " +
               "JOIN dentists d ON a.dentist_id = d.dentist_id " +
               "JOIN treatments t ON a.treatment_id = t.treatment_id";
    }

    private Invoice mapJoinedResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setAppointmentId(rs.getInt("appointment_id"));
        inv.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        inv.setTreatmentCost(rs.getBigDecimal("treatment_cost"));
        inv.setDiscountPercentage(rs.getBigDecimal("discount_percentage"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setCreatedAt(rs.getTimestamp("created_at"));

        // Presentation / Joined details
        inv.setAppointmentNumber(rs.getString("appointment_number"));
        inv.setAppointmentDate(rs.getString("appointment_date"));
        inv.setAppointmentTime(rs.getString("appointment_time"));
        inv.setPatientName(rs.getString("patient_name"));
        inv.setPatientContact(rs.getString("patient_contact"));
        inv.setPatientAddress(rs.getString("patient_address"));
        inv.setDentistName(rs.getString("dentist_name"));
        inv.setTreatmentName(rs.getString("treatment_name"));

        return inv;
    }
}
