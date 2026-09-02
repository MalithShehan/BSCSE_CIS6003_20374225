package dao;

import config.DatabaseConnection;
import model.ReportItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Decision-Support and Executive Reporting.
 * Reads directly from optimized MySQL database views.
 */
public class ReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ReportDAO.class.getName());

    /**
     * Retrieves daily appointment distribution and revenue breakdown from View `Daily_Appointment_Report`.
     */
    public List<ReportItem> getDailyAppointmentReport() throws SQLException {
        List<ReportItem> reports = new ArrayList<>();
        String sql = "SELECT appointment_date, dentist_name, specialization, total_scheduled_appointments, " +
                     "completed_appointments, pending_appointments, cancelled_appointments, daily_revenue_generated " +
                     "FROM Daily_Appointment_Report";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ReportItem item = new ReportItem();
                item.setAppointmentDate(rs.getDate("appointment_date"));
                item.setDentistName(rs.getString("dentist_name"));
                item.setSpecialization(rs.getString("specialization"));
                item.setTotalScheduledAppointments(rs.getInt("total_scheduled_appointments"));
                item.setCompletedAppointments(rs.getInt("completed_appointments"));
                item.setPendingAppointments(rs.getInt("pending_appointments"));
                item.setCancelledAppointments(rs.getInt("cancelled_appointments"));
                item.setDailyRevenueGenerated(rs.getBigDecimal("daily_revenue_generated"));
                reports.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying Daily_Appointment_Report view", e);
            throw e;
        }
        return reports;
    }

    /**
     * Retrieves monthly revenue aggregation from View `Monthly_Revenue_Report`.
     */
    public List<ReportItem> getMonthlyRevenueReport() throws SQLException {
        List<ReportItem> reports = new ArrayList<>();
        String sql = "SELECT revenue_month, total_invoices_issued, total_consultation_fees, " +
                     "total_treatment_costs, total_discounts_granted, net_revenue " +
                     "FROM Monthly_Revenue_Report";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ReportItem item = new ReportItem();
                item.setRevenueMonth(rs.getString("revenue_month"));
                item.setTotalInvoicesIssued(rs.getInt("total_invoices_issued"));
                item.setTotalConsultationFees(rs.getBigDecimal("total_consultation_fees"));
                item.setTotalTreatmentCosts(rs.getBigDecimal("total_treatment_costs"));
                item.setTotalDiscountsGranted(rs.getBigDecimal("total_discounts_granted"));
                item.setNetRevenue(rs.getBigDecimal("net_revenue"));
                reports.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying Monthly_Revenue_Report view", e);
            throw e;
        }
        return reports;
    }

    /**
     * Retrieves practitioner workload and revenue generation metrics from View `Dentist_Performance_Report`.
     */
    public List<ReportItem> getDentistPerformanceReport() throws SQLException {
        List<ReportItem> reports = new ArrayList<>();
        String sql = "SELECT dentist_id, dentist_name, specialization, consultation_fee, " +
                     "unique_patients_served, total_assigned_appointments, successful_treatments, total_revenue_generated " +
                     "FROM Dentist_Performance_Report";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ReportItem item = new ReportItem();
                item.setDentistId(rs.getInt("dentist_id"));
                item.setDentistName(rs.getString("dentist_name"));
                item.setSpecialization(rs.getString("specialization"));
                item.setConsultationFee(rs.getBigDecimal("consultation_fee"));
                item.setUniquePatientsServed(rs.getInt("unique_patients_served"));
                item.setTotalAssignedAppointments(rs.getInt("total_assigned_appointments"));
                item.setSuccessfulTreatments(rs.getInt("successful_treatments"));
                item.setTotalRevenueGenerated(rs.getBigDecimal("total_revenue_generated"));
                reports.add(item);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying Dentist_Performance_Report view", e);
            throw e;
        }
        return reports;
    }
}
