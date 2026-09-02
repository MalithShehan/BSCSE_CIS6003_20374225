package service;

import dao.AppointmentDAO;
import dao.InvoiceDAO;
import model.Appointment;
import model.Invoice;
import util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class handling invoice calculation, duplicate billing prevention,
 * stored procedure dispatching, and receipt notifications.
 */
public class BillingService {

    private static final Logger LOGGER = Logger.getLogger(BillingService.class.getName());

    private final InvoiceDAO invoiceDAO;
    private final AppointmentDAO appointmentDAO;
    private final NotificationService smsNotifier;

    /**
     * Default constructor.
     */
    public BillingService() {
        this.invoiceDAO = new InvoiceDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.smsNotifier = NotificationFactory.getNotificationService(NotificationFactory.Channel.SMS);
    }

    /**
     * Testing constructor for Mockito.
     */
    public BillingService(InvoiceDAO invoiceDAO, AppointmentDAO appointmentDAO, NotificationService smsNotifier) {
        this.invoiceDAO = invoiceDAO;
        this.appointmentDAO = appointmentDAO;
        this.smsNotifier = smsNotifier;
    }

    /**
     * Generates an official invoice for an appointment.
     * Enforces business rules:
     * 1. Appointment must exist.
     * 2. Appointment must NOT be CANCELLED.
     * 3. An invoice must NOT already exist for this appointment (idempotency).
     * 4. Discount must be between 0.00% and 100.00%.
     * 5. Invokes MySQL Stored Procedure GenerateBillForAppointment.
     * 6. Marks appointment as COMPLETED.
     * 7. Sends payment confirmation notification.
     *
     * @param appointmentId       Target appointment ID
     * @param discountPercentage  Optional discount percentage (e.g. 10.0 for 10%)
     * @param paymentMethod       Payment method (CASH, CREDIT_CARD, etc.)
     * @return Fully populated Invoice object
     */
    public Invoice generateBill(int appointmentId, BigDecimal discountPercentage, String paymentMethod) throws SQLException {
        if (appointmentId <= 0) {
            throw new IllegalArgumentException("Valid appointment ID is required.");
        }

        // 1. Fetch and validate Appointment
        Appointment appointment = appointmentDAO.findById(appointmentId);
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment not found with ID: " + appointmentId);
        }

        if ("CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Cannot generate bill for a cancelled appointment.");
        }

        // 2. Prevent duplicate billing
        Invoice existingInvoice = invoiceDAO.findByAppointmentId(appointmentId);
        if (existingInvoice != null) {
            throw new IllegalStateException("Bill has already been generated for this appointment. Invoice Number: " 
                    + existingInvoice.getInvoiceNumber());
        }

        // 3. Validate Discount
        if (discountPercentage != null) {
            if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("Discount percentage must be between 0.00% and 100.00%.");
            }
        } else {
            discountPercentage = BigDecimal.ZERO;
        }

        String normalizedPaymentMethod = ValidationUtil.isNotEmpty(paymentMethod) ? paymentMethod.trim().toUpperCase() : "CASH";

        // 4. Execute Stored Procedure via DAO
        Invoice generatedInvoice = invoiceDAO.generateBill(appointmentId, discountPercentage, normalizedPaymentMethod);

        // 5. Send Notification
        try {
            if (smsNotifier != null && appointment.getPatientContact() != null) {
                smsNotifier.sendBillingNotification(appointment.getAppointmentNumber(), 
                        appointment.getPatientContact(), 
                        generatedInvoice.getTotalAmount().doubleValue());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Simulated billing notification failed", e);
        }

        LOGGER.info("Invoice generated successfully: " + generatedInvoice.getInvoiceNumber() 
                + " for Appointment " + appointment.getAppointmentNumber());
        return generatedInvoice;
    }

    /**
     * Retrieves an invoice by appointment reference number (e.g., SDC-2026-0001).
     */
    public Invoice getInvoiceByAppointmentNumber(String appointmentNumber) throws SQLException {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            throw new IllegalArgumentException("Appointment number is required.");
        }

        Invoice invoice = invoiceDAO.findByAppointmentNumber(appointmentNumber.trim().toUpperCase());
        if (invoice == null) {
            throw new IllegalArgumentException("No invoice found for appointment reference: " + appointmentNumber);
        }
        return invoice;
    }

    /**
     * Retrieves an invoice by its invoice ID.
     */
    public Invoice getInvoiceById(int invoiceId) throws SQLException {
        Invoice invoice = invoiceDAO.findById(invoiceId);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice ID " + invoiceId + " not found.");
        }
        return invoice;
    }

    /**
     * Retrieves all invoices for auditing.
     */
    public List<Invoice> getAllInvoices() throws SQLException {
        return invoiceDAO.findAll();
    }
}
