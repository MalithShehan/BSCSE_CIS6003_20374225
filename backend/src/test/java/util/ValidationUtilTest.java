package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil Unit Tests")
public class ValidationUtilTest {

    @Nested
    @DisplayName("String Emptiness Validation")
    class StringValidationTests {

        @Test
        @DisplayName("Should return true for non-empty string")
        void testIsNotEmpty_ValidString() {
            assertTrue(ValidationUtil.isNotEmpty("Sunrise Dental"));
        }

        @Test
        @DisplayName("Should return false for null string")
        void testIsNotEmpty_NullString() {
            assertFalse(ValidationUtil.isNotEmpty(null));
        }

        @Test
        @DisplayName("Should return false for empty or whitespace-only string")
        void testIsNotEmpty_WhitespaceString() {
            assertFalse(ValidationUtil.isNotEmpty("   "));
            assertFalse(ValidationUtil.isNotEmpty(""));
        }
    }

    @Nested
    @DisplayName("Phone Number Validation")
    class PhoneValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"0771234567", "+94771234567", "0112345678", "+94 77 123 4567", "071-2345678"})
        @DisplayName("Should accept valid phone formats")
        void testIsValidPhoneNumber_ValidFormats(String phone) {
            assertTrue(ValidationUtil.isValidPhoneNumber(phone));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "phone_number", "", "abc1234567"})
        @DisplayName("Should reject invalid phone formats")
        void testIsValidPhoneNumber_InvalidFormats(String phone) {
            assertFalse(ValidationUtil.isValidPhoneNumber(phone));
        }
    }

    @Nested
    @DisplayName("Email Address Validation")
    class EmailValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {"patient@example.com", "doctor.ruwan@sunrisedental.lk", "test.user+tag@domain.co.uk"})
        @DisplayName("Should accept valid email formats")
        void testIsValidEmail_Valid(String email) {
            assertTrue(ValidationUtil.isValidEmail(email));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "user@.com", "@domain.com"})
        @DisplayName("Should reject invalid email formats")
        void testIsValidEmail_Invalid(String email) {
            assertFalse(ValidationUtil.isValidEmail(email));
        }

        @Test
        @DisplayName("Should allow null or empty email (optional field)")
        void testIsValidEmail_Optional() {
            assertTrue(ValidationUtil.isValidEmail(null));
            assertTrue(ValidationUtil.isValidEmail(""));
        }
    }

    @Nested
    @DisplayName("Appointment Date & Time Validation")
    class DateTimeValidationTests {

        @Test
        @DisplayName("Should accept today's date")
        void testIsFutureOrTodayDate_Today() {
            Date today = Date.valueOf(LocalDate.now());
            assertTrue(ValidationUtil.isFutureOrTodayDate(today));
        }

        @Test
        @DisplayName("Should accept tomorrow's date")
        void testIsFutureOrTodayDate_Future() {
            Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));
            assertTrue(ValidationUtil.isFutureOrTodayDate(tomorrow));
        }

        @Test
        @DisplayName("Should reject yesterday's date")
        void testIsFutureOrTodayDate_Past() {
            Date yesterday = Date.valueOf(LocalDate.now().minusDays(1));
            assertFalse(ValidationUtil.isFutureOrTodayDate(yesterday));
        }

        @Test
        @DisplayName("Should accept time within clinic hours (08:00 to 17:00)")
        void testIsWithinClinicHours_Valid() {
            assertTrue(ValidationUtil.isWithinClinicHours(Time.valueOf("08:00:00")));
            assertTrue(ValidationUtil.isWithinClinicHours(Time.valueOf("12:30:00")));
            assertTrue(ValidationUtil.isWithinClinicHours(Time.valueOf("17:00:00")));
        }

        @Test
        @DisplayName("Should reject time outside clinic hours")
        void testIsWithinClinicHours_Invalid() {
            assertFalse(ValidationUtil.isWithinClinicHours(Time.valueOf("07:59:00"))); // Early
            assertFalse(ValidationUtil.isWithinClinicHours(Time.valueOf("17:01:00"))); // Late
            assertFalse(ValidationUtil.isWithinClinicHours(Time.valueOf("21:00:00"))); // Night
        }
    }

    @Nested
    @DisplayName("Appointment Number & Discount Validation")
    class IdentifierAndFinancialTests {

        @Test
        @DisplayName("Should validate correct SDC-YYYY-XXXX format")
        void testIsValidAppointmentNumber() {
            assertTrue(ValidationUtil.isValidAppointmentNumber("SDC-2026-0001"));
            assertTrue(ValidationUtil.isValidAppointmentNumber("SDC-2024-9999"));
            assertFalse(ValidationUtil.isValidAppointmentNumber("APP-2026-0001"));
            assertFalse(ValidationUtil.isValidAppointmentNumber("SDC20260001"));
            assertFalse(ValidationUtil.isValidAppointmentNumber(""));
        }

        @Test
        @DisplayName("Should validate discount percentage boundaries (0 to 100)")
        void testIsValidDiscount() {
            assertTrue(ValidationUtil.isValidDiscount(0.0));
            assertTrue(ValidationUtil.isValidDiscount(15.5));
            assertTrue(ValidationUtil.isValidDiscount(100.0));
            assertFalse(ValidationUtil.isValidDiscount(-5.0));
            assertFalse(ValidationUtil.isValidDiscount(105.0));
        }
    }
}
