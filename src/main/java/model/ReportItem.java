package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * Data Transfer Objects for Decision-Support Reporting Views.
 */
public class ReportItem implements Serializable {
    private static final long serialVersionUID = 1L;

    // Daily Report Fields
    private Date appointmentDate;
    private String dentistName;
    private String specialization;
    private int totalScheduledAppointments;
    private int completedAppointments;
    private int pendingAppointments;
    private int cancelledAppointments;
    private BigDecimal dailyRevenueGenerated;

    // Monthly Report Fields
    private String revenueMonth; // YYYY-MM
    private int totalInvoicesIssued;
    private BigDecimal totalConsultationFees;
    private BigDecimal totalTreatmentCosts;
    private BigDecimal totalDiscountsGranted;
    private BigDecimal netRevenue;

    // Dentist Performance Report Fields
    private int dentistId;
    private BigDecimal consultationFee;
    private int uniquePatientsServed;
    private int totalAssignedAppointments;
    private int successfulTreatments;
    private BigDecimal totalRevenueGenerated;

    public ReportItem() {
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getTotalScheduledAppointments() {
        return totalScheduledAppointments;
    }

    public void setTotalScheduledAppointments(int totalScheduledAppointments) {
        this.totalScheduledAppointments = totalScheduledAppointments;
    }

    public int getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(int completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public int getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(int pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }

    public int getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(int cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public BigDecimal getDailyRevenueGenerated() {
        return dailyRevenueGenerated;
    }

    public void setDailyRevenueGenerated(BigDecimal dailyRevenueGenerated) {
        this.dailyRevenueGenerated = dailyRevenueGenerated;
    }

    public String getRevenueMonth() {
        return revenueMonth;
    }

    public void setRevenueMonth(String revenueMonth) {
        this.revenueMonth = revenueMonth;
    }

    public int getTotalInvoicesIssued() {
        return totalInvoicesIssued;
    }

    public void setTotalInvoicesIssued(int totalInvoicesIssued) {
        this.totalInvoicesIssued = totalInvoicesIssued;
    }

    public BigDecimal getTotalConsultationFees() {
        return totalConsultationFees;
    }

    public void setTotalConsultationFees(BigDecimal totalConsultationFees) {
        this.totalConsultationFees = totalConsultationFees;
    }

    public BigDecimal getTotalTreatmentCosts() {
        return totalTreatmentCosts;
    }

    public void setTotalTreatmentCosts(BigDecimal totalTreatmentCosts) {
        this.totalTreatmentCosts = totalTreatmentCosts;
    }

    public BigDecimal getTotalDiscountsGranted() {
        return totalDiscountsGranted;
    }

    public void setTotalDiscountsGranted(BigDecimal totalDiscountsGranted) {
        this.totalDiscountsGranted = totalDiscountsGranted;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public int getUniquePatientsServed() {
        return uniquePatientsServed;
    }

    public void setUniquePatientsServed(int uniquePatientsServed) {
        this.uniquePatientsServed = uniquePatientsServed;
    }

    public int getTotalAssignedAppointments() {
        return totalAssignedAppointments;
    }

    public void setTotalAssignedAppointments(int totalAssignedAppointments) {
        this.totalAssignedAppointments = totalAssignedAppointments;
    }

    public int getSuccessfulTreatments() {
        return successfulTreatments;
    }

    public void setSuccessfulTreatments(int successfulTreatments) {
        this.successfulTreatments = successfulTreatments;
    }

    public BigDecimal getTotalRevenueGenerated() {
        return totalRevenueGenerated;
    }

    public void setTotalRevenueGenerated(BigDecimal totalRevenueGenerated) {
        this.totalRevenueGenerated = totalRevenueGenerated;
    }
}
