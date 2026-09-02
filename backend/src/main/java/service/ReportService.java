package service;

import dao.ReportDAO;
import model.ReportItem;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class aggregating and serving decision-support analytics from MySQL Views.
 */
public class ReportService {

    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    /**
     * Retrieves daily appointment workload and daily revenue figures.
     */
    public List<ReportItem> getDailyReport() throws SQLException {
        try {
            return reportDAO.getDailyAppointmentReport();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating daily appointment report", e);
            throw e;
        }
    }

    /**
     * Retrieves monthly financial overview and revenue figures.
     */
    public List<ReportItem> getMonthlyRevenueReport() throws SQLException {
        try {
            return reportDAO.getMonthlyRevenueReport();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating monthly revenue report", e);
            throw e;
        }
    }

    /**
     * Retrieves dentist performance, patient counts, and revenue attribution.
     */
    public List<ReportItem> getDentistPerformanceReport() throws SQLException {
        try {
            return reportDAO.getDentistPerformanceReport();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating dentist performance report", e);
            throw e;
        }
    }

    /**
     * Retrieves aggregated dashboard KPI metrics.
     */
    public Map<String, Object> getDashboardSummary() throws SQLException {
        Map<String, Object> summary = new HashMap<>();
        List<ReportItem> daily = getDailyReport();
        List<ReportItem> monthly = getMonthlyRevenueReport();
        List<ReportItem> dentists = getDentistPerformanceReport();

        int totalAppointments = 0;
        int totalCompleted = 0;
        for (ReportItem d : daily) {
            totalAppointments += d.getTotalScheduledAppointments();
            totalCompleted += d.getCompletedAppointments();
        }

        summary.put("dailyBreakdown", daily);
        summary.put("monthlyFinancials", monthly);
        summary.put("dentistPerformance", dentists);
        summary.put("totalRecordedAppointments", totalAppointments);
        summary.put("totalCompletedTreatments", totalCompleted);

        return summary;
    }
}
