package service;

import dao.UserDAO;
import model.User;
import util.PasswordUtil;
import util.ValidationUtil;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class handling user authentication, credentials validation, and authorization.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private final UserDAO userDAO;

    /**
     * Default constructor initializing default UserDAO.
     */
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Dependency injection constructor for Unit Testing with Mockito.
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user against stored BCrypt credentials.
     *
     * @param username Raw username string
     * @param password Raw password string
     * @return Authenticated User object if valid and active
     * @throws IllegalArgumentException on missing inputs
     * @throws SecurityException        on invalid credentials or inactive account
     * @throws SQLException             on database communication failure
     */
    public User authenticate(String username, String password) throws SQLException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            throw new IllegalArgumentException("Username and password are required.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            LOGGER.warning("Authentication failed: User not found -> " + username);
            throw new SecurityException("Invalid username or password.");
        }

        if (!user.isActive()) {
            LOGGER.warning("Authentication failed: Account disabled -> " + username);
            throw new SecurityException("Your account is currently inactive. Please contact administration.");
        }

        boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            LOGGER.warning("Authentication failed: Invalid password for user -> " + username);
            throw new SecurityException("Invalid username or password.");
        }

        LOGGER.info("Authentication successful for user: " + username + " (Role: " + user.getRole() + ")");
        return user;
    }

    /**
     * Registers a new system user with BCrypt hashed password.
     */
    public User registerUser(String username, String rawPassword, String fullName, String role) throws SQLException {
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(rawPassword) || !ValidationUtil.isNotEmpty(fullName)) {
            throw new IllegalArgumentException("All registration fields are required.");
        }

        User existing = userDAO.findByUsername(username.trim());
        if (existing != null) {
            throw new IllegalArgumentException("Username already exists. Please choose a different username.");
        }

        String hashedPassword = PasswordUtil.hashPassword(rawPassword);
        User newUser = new User(username.trim(), hashedPassword, fullName.trim(), role != null ? role.toUpperCase() : "RECEPTIONIST");

        int userId = userDAO.createUser(newUser);
        newUser.setUserId(userId);
        return newUser;
    }

    /**
     * Fetches user profile by ID.
     */
    public User getUserProfile(int userId) throws SQLException {
        return userDAO.findById(userId);
    }
}
