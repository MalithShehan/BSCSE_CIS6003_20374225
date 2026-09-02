package service;

import dao.AppointmentDAO;
import dao.DentistDAO;
import dao.PatientDAO;
import dao.TreatmentDAO;
import model.Appointment;
import model.Dentist;
import model.Patient;
import model.Treatment;
import util.AppointmentNumberGenerator;
import util.ValidationUtil;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class orchestrating appointment registration, validations,
 * double-booking prevention, searching, and notification triggers.
 */
public class AppointmentService {

    private static final Logger LOGGER = Logger.getLogger(AppointmentService.class.getName());

    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;
    private final NotificationService emailNotifier;
    private final NotificationService smsNotifier;

    /**
     * Default constructor with production dependencies.
     */
    public AppointmentService() {
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.dentistDAO = new DentistDAO();
        this.treatmentDAO = new TreatmentDAO();
        this.emailNotifier = NotificationFactory.getNotificationService(NotificationFactory.Channel.EMAIL);
        this.smsNotifier = NotificationFactory.getNotificationService(NotificationFactory.Channel.SMS);
    }

    /**
     * Constructor for Unit Testing with Mockito.
     */
    public AppointmentService(AppointmentDAO appointmentDAO, PatientDAO patientDAO,
                              DentistDAO dentistDAO, TreatmentDAO treatmentDAO,
                              NotificationService emailNotifier, NotificationService smsNotifier) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
        this.emailNotifier = emailNotifier;
        this.smsNotifier = smsNotifier;
    }

    /**
     * Registers a new appointment.
     * Enforces complete business validations:
     * 1. Patient information checks (name, phone format).
     * 2. Dentist & Treatment existence checks.
     * 3. Date check (cannot be in the past).
     * 4. Time check (must fall between 08:00 and 17:00).
     * 5. Double-booking check for the dentist.
     * 6. Unique appointment number generation (SDC-YYYY-XXXX).
     * 7. SMS/Email notification dispatch.
     */
    public Appointment bookAppointment(String patientName, String patientAddress, String patientContact,
                                       String patientEmail, int dentistId, int treatmentId,
                                       Date appointmentDate, Time appointmentTime, String notes) throws SQLException {

        // 1. Validate Patient inputs
        if (!ValidationUtil.isNotEmpty(patientName)) {
            throw new IllegalArgumentException("Patient name is required.");
        }
        if (!ValidationUtil.isValidPhoneNumber(patientContact)) {
            throw new IllegalArgumentException("Valid contact number is required (e.g., 0771234567 or +94771234567).");
        }
        if (!ValidationUtil.isValidEmail(patientEmail)) {
            throw new IllegalArgumentException("Invalid email address format.");
        }

        // 2. Validate Dentist
        Dentist dentist = dentistDAO.findById(dentistId);
        if (dentist == null || !dentist.isActive()) {
            throw new IllegalArgumentException("Selected dentist does not exist or is currently inactive.");
        }

        // 3. Validate Treatment
        Treatment treatment = treatmentDAO.findById(treatmentId);
        if (treatment == null || !treatment.isActive()) {
            throw new IllegalArgumentException("Selected treatment is invalid or inactive.");
        }

        // 4. Validate Date
        if (!ValidationUtil.isFutureOrTodayDate(appointmentDate)) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        // 5. Validate Time (Clinic operating hours: 08:00 to 17:00)
        if (!ValidationUtil.isWithinClinicHours(appointmentTime)) {
            throw new IllegalArgumentException("Appointment time must be within clinic hours (08:00 to 17:00).");
        }

        // 6. Check Double-Booking conflict
        if (appointmentDAO.isDoubleBooked(dentistId, appointmentDate, appointmentTime, null)) {
            throw new IllegalStateException("Conflict: " + dentist.getDentistName() + 
                    " already has a scheduled appointment on " + appointmentDate + " at " + appointmentTime + ".");
        }

        // 7. Persist or Fetch Patient
        Patient patient = new Patient(patientName.trim(), 
                patientAddress != null ? patientAddress.trim() : "Colombo",
                patientContact.trim(), 
                patientEmail != null ? patientEmail.trim() : null);
        int patientId = patientDAO.createPatient(patient);

        // 8. Generate Unique Sequential Appointment Number (SDC-YYYY-XXXX)
        int currentYear = LocalDate.now().getYear();
        int nextSeq = appointmentDAO.getNextSequenceNumber(currentYear);
        String appointmentNumber = AppointmentNumberGenerator.format(currentYear, nextSeq);

        // 9. Persist Appointment
        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber(appointmentNumber);
        appointment.setPatientId(patientId);
        appointment.setDentistId(dentistId);
        appointment.setTreatmentId(treatmentId);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus("SCHEDULED");
        appointment.setNotes(notes);

        int appointmentId = appointmentDAO.createAppointment(appointment);
        appointment.setAppointmentId(appointmentId);

        // Populate presentation details
        appointment.setPatientName(patientName);
        appointment.setPatientContact(patientContact);
        appointment.setPatientAddress(patientAddress);
        appointment.setPatientEmail(patientEmail);
        appointment.setDentistName(dentist.getDentistName());
        appointment.setDentistSpecialization(dentist.getSpecialization());
        appointment.setDentistFee(dentist.getConsultationFee());
        appointment.setTreatmentName(treatment.getTreatmentName());
        appointment.setTreatmentCost(treatment.getCost());
        appointment.setTotalEstimatedCost(dentist.getConsultationFee().add(treatment.getCost()));

        // 10. Trigger Simulated Notifications (Observer / Strategy)
        try {
            if (smsNotifier != null) {
                smsNotifier.sendAppointmentNotification(appointment);
            }
            if (emailNotifier != null && patientEmail != null) {
                emailNotifier.sendAppointmentNotification(appointment);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Simulated notification dispatch failed", e);
        }

        LOGGER.info("Appointment booked successfully: " + appointmentNumber + " for patient " + patientName);
        return appointment;
    }

    /**
     * Searches appointment by appointment number with format validation.
     */
    public Appointment getAppointmentByNumber(String appointmentNumber) throws SQLException {
        if (!ValidationUtil.isNotEmpty(appointmentNumber)) {
            throw new IllegalArgumentException("Appointment number cannot be empty.");
        }

        String cleanedNum = appointmentNumber.trim().toUpperCase();
        if (!ValidationUtil.isValidAppointmentNumber(cleanedNum)) {
            throw new IllegalArgumentException("Invalid appointment number format. Expected format: SDC-YYYY-XXXX (e.g., SDC-2026-0001).");
        }

        Appointment app = appointmentDAO.findByAppointmentNumber(cleanedNum);
        if (app == null) {
            throw new IllegalArgumentException("No appointment found matching reference: " + cleanedNum);
        }
        return app;
    }

    /**
     * Retrieves all appointments with optional filters.
     */
    public List<Appointment> getAppointments(String date, Integer dentistId, String status) throws SQLException {
        return appointmentDAO.findAllFiltered(date, dentistId, status);
    }

    /**
     * Updates appointment status (e.g. CANCELLED or COMPLETED).
     */
    public boolean updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException {
        if (!ValidationUtil.isNotEmpty(newStatus)) {
            throw new IllegalArgumentException("Status value is required.");
        }
        String statusUpper = newStatus.trim().toUpperCase();
        if (!statusUpper.equals("SCHEDULED") && !statusUpper.equals("COMPLETED") && !statusUpper.equals("CANCELLED")) {
            throw new IllegalArgumentException("Invalid status. Allowed values: SCHEDULED, COMPLETED, CANCELLED.");
        }

        Appointment existing = appointmentDAO.findById(appointmentId);
        if (existing == null) {
            throw new IllegalArgumentException("Appointment ID " + appointmentId + " not found.");
        }

        return appointmentDAO.updateStatus(appointmentId, statusUpper);
    }
}
