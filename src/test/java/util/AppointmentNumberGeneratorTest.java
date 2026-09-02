package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppointmentNumberGenerator Unit Tests")
public class AppointmentNumberGeneratorTest {

    @Test
    @DisplayName("Should generate standard format with padding for small sequences")
    void testFormat_Standard() {
        String appNum = AppointmentNumberGenerator.format(2026, 1);
        assertEquals("SDC-2026-0001", appNum);
    }

    @Test
    @DisplayName("Should format 4-digit sequences without losing prefix")
    void testFormat_LargeSequence() {
        String appNum = AppointmentNumberGenerator.format(2026, 1234);
        assertEquals("SDC-2026-1234", appNum);
    }

    @Test
    @DisplayName("Should auto-generate for current year")
    void testGenerateForCurrentYear() {
        int currentYear = LocalDate.now().getYear();
        String appNum = AppointmentNumberGenerator.generateForCurrentYear(42);
        assertEquals("SDC-" + currentYear + "-0042", appNum);
    }

    @Test
    @DisplayName("Should normalize invalid sequence numbers to 1")
    void testFormat_NegativeSequence() {
        String appNum = AppointmentNumberGenerator.format(2026, -5);
        assertEquals("SDC-2026-0001", appNum);
    }
}
