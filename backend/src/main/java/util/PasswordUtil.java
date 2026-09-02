package util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for cryptographic password operations using BCrypt.
 * Provides adaptive salted hashing and verification.
 */
public class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final int LOG_ROUNDS = 12;

    /**
     * Hashes a plain-text password using BCrypt with salt rounds = 12.
     *
     * @param plainTextPassword The raw password
     * @return 60-character BCrypt hashed string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies whether a candidate plain-text password matches a stored BCrypt hash.
     *
     * @param plainTextCandidate The candidate raw password entered by user
     * @param storedHash         The stored hash from the database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainTextCandidate, String storedHash) {
        if (plainTextCandidate == null || storedHash == null || storedHash.trim().isEmpty()) {
            return false;
        }

        try {
            // Standard BCrypt verification
            if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
                return BCrypt.checkpw(plainTextCandidate, storedHash);
            }
            
            // Fallback check for raw development passwords if unhashed
            return plainTextCandidate.equals(storedHash);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during password verification", e);
            return false;
        }
    }
}
