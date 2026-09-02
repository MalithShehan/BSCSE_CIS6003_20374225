package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil Cryptographic Unit Tests")
public class PasswordUtilTest {

    @Test
    @DisplayName("Should hash plain-text password using BCrypt format")
    void testHashPassword_Format() {
        String plain = "Admin@123";
        String hash = PasswordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertEquals(60, hash.length(), "BCrypt hash must be 60 characters in length");
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$"), "Hash should use work factor 12");
    }

    @Test
    @DisplayName("Should generate unique salts for identical passwords")
    void testHashPassword_SaltUniqueness() {
        String plain = "SecretPassword!123";
        String hash1 = PasswordUtil.hashPassword(plain);
        String hash2 = PasswordUtil.hashPassword(plain);

        assertNotEquals(hash1, hash2, "BCrypt must use salt randomization for every hash invocation");
    }

    @Test
    @DisplayName("Should successfully verify correct password against hash")
    void testVerifyPassword_Success() {
        String plain = "Reception@123";
        String hash = PasswordUtil.hashPassword(plain);

        assertTrue(PasswordUtil.verifyPassword(plain, hash));
    }

    @Test
    @DisplayName("Should reject incorrect password")
    void testVerifyPassword_Failure() {
        String plain = "Dentist@123";
        String wrong = "WrongPassword@123";
        String hash = PasswordUtil.hashPassword(plain);

        assertFalse(PasswordUtil.verifyPassword(wrong, hash));
    }

    @Test
    @DisplayName("Should throw exception when hashing empty or null password")
    void testHashPassword_EmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
    }
}
