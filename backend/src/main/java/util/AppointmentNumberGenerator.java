package util;

import java.time.LocalDate;

/**
 * Utility for generating standardized and unique appointment numbers.
 * Format: SDC-YYYY-XXXX (e.g., SDC-2026-0001)
 */
public class AppointmentNumberGenerator {

    private static final String PREFIX = "SDC";

    /**
     * Generates a formatted appointment number given the year and sequence.
     *
     * @param year           The appointment year (e.g. 2026)
     * @param sequenceNumber The incremental sequence number (e.g. 1)
     * @return Formatted appointment number (e.g., SDC-2026-0001)
     */
    public static String format(int year, int sequenceNumber) {
        if (year < 2000) {
            year = LocalDate.now().getYear();
        }
        if (sequenceNumber < 1) {
            sequenceNumber = 1;
        }
        return String.format("%s-%d-%04d", PREFIX, year, sequenceNumber);
    }

    /**
     * Overload generating appointment number for current year.
     *
     * @param sequenceNumber Incremental sequence number
     * @return Formatted string
     */
    public static String generateForCurrentYear(int sequenceNumber) {
        return format(LocalDate.now().getYear(), sequenceNumber);
    }
}
