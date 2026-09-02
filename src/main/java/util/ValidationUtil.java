package util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Comprehensive Validation Utility for Business Logic and Controller layers.
 */
public class ValidationUtil {

    // Regex Patterns
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\s\\-\\(\\)]{9,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern APPOINTMENT_NUM_PATTERN = Pattern.compile("^SDC-\\d{4}-\\d{4}$");

    // Clinic Operating Hours: 08:00 to 17:00
    public static final LocalTime CLINIC_OPEN_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLINIC_CLOSE_TIME = LocalTime.of(17, 0);

    /**
     * Checks if a string is non-null and not blank.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates contact number format.
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates email address syntax (optional field).
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return true; // Optional field
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates that appointment date is not in the past.
     */
    public static boolean isFutureOrTodayDate(Date date) {
        if (date == null) {
            return false;
        }
        LocalDate appointmentDate = date.toLocalDate();
        LocalDate today = LocalDate.now();
        return !appointmentDate.isBefore(today);
    }

    /**
     * Validates that appointment time falls strictly within clinic operating hours (08:00 to 17:00).
     */
    public static boolean isWithinClinicHours(Time time) {
        if (time == null) {
            return false;
        }
        LocalTime appointmentTime = time.toLocalTime();
        return !appointmentTime.isBefore(CLINIC_OPEN_TIME) && !appointmentTime.isAfter(CLINIC_CLOSE_TIME);
    }

    /**
     * Validates appointment number format (SDC-YYYY-XXXX).
     */
    public static boolean isValidAppointmentNumber(String appointmentNumber) {
        if (!isNotEmpty(appointmentNumber)) {
            return false;
        }
        return APPOINTMENT_NUM_PATTERN.matcher(appointmentNumber.trim()).matches();
    }

    /**
     * Validates discount percentage range (0.00 to 100.00).
     */
    public static boolean isValidDiscount(double discount) {
        return discount >= 0.0 && discount <= 100.0;
    }
}
