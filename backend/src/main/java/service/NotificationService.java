package service;

import model.Appointment;

import java.util.logging.Logger;

/**
 * Interface defining notification dispatch operations.
 * Design Patterns:
 * - Strategy Pattern: Multiple communication strategies (Email, SMS)
 * - Factory Pattern: Instantiated via NotificationFactory
 */
public interface NotificationService {

    /**
     * Sends an appointment confirmation or status update notification.
     *
     * @param appointment The scheduled or updated appointment
     * @return true if notification dispatched successfully
     */
    boolean sendAppointmentNotification(Appointment appointment);

    /**
     * Sends an invoice/receipt generation notification.
     *
     * @param appointmentNumber The appointment identifier
     * @param recipientContact  Recipient phone or email
     * @param amount            Invoice total amount
     * @return true if dispatched
     */
    boolean sendBillingNotification(String appointmentNumber, String recipientContact, double amount);
}

/**
 * Simulated Email Notification Service.
 */
class EmailNotificationService implements NotificationService {
    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());

    @Override
    public boolean sendAppointmentNotification(Appointment appointment) {
        String email = appointment.getPatientEmail() != null ? appointment.getPatientEmail() : "patient@example.com";
        LOGGER.info(String.format("[EMAIL SERVICE] To: %s | Subject: Sunrise Dental Clinic Appointment Confirmation | " +
                "Ref: %s, Date: %s at %s with %s. Treatment: %s",
                email, appointment.getAppointmentNumber(), appointment.getAppointmentDate(),
                appointment.getAppointmentTime(), appointment.getDentistName(), appointment.getTreatmentName()));
        return true;
    }

    @Override
    public boolean sendBillingNotification(String appointmentNumber, String recipientContact, double amount) {
        LOGGER.info(String.format("[EMAIL SERVICE] To: %s | Subject: Payment Receipt for %s | Total Paid: LKR %.2f. Thank you!",
                recipientContact, appointmentNumber, amount));
        return true;
    }
}

/**
 * Simulated SMS Notification Service.
 */
class SmsNotificationService implements NotificationService {
    private static final Logger LOGGER = Logger.getLogger(SmsNotificationService.class.getName());

    @Override
    public boolean sendAppointmentNotification(Appointment appointment) {
        LOGGER.info(String.format("[SMS GATEWAY] To: %s | Sunrise Dental: Your appointment %s is confirmed for %s at %s with %s.",
                appointment.getPatientContact(), appointment.getAppointmentNumber(),
                appointment.getAppointmentDate(), appointment.getAppointmentTime(), appointment.getDentistName()));
        return true;
    }

    @Override
    public boolean sendBillingNotification(String appointmentNumber, String recipientContact, double amount) {
        LOGGER.info(String.format("[SMS GATEWAY] To: %s | Sunrise Dental: Invoice for %s settled. Amount: LKR %.2f.",
                recipientContact, appointmentNumber, amount));
        return true;
    }
}
