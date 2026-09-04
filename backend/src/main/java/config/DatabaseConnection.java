package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton Database Connection Manager for Sunrise Dental Clinic.
 * 
 * Design Pattern: Singleton Pattern (Thread-safe Double-Checked Locking)
 * 
 * Features:
 * 1. Automatic loading of MySQL 8 Connector/J driver.
 * 2. Multi-tier configuration lookup:
 * - Priority 1: Environment variables (DB_URL, DB_USER, DB_PASSWORD)
 * - Priority 2: db.properties file in classpath
 * - Priority 3: Default Java constants (fallback)
 * 3. Connection factory method for standard JDBC DAO try-with-resources blocks.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Fallback Default Constants
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/sunrise_dental_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "Malith@1001";

    // Configuration Properties
    private String driver;
    private String url;
    private String username;
    private String password;

    // Volatile Singleton Instance
    private static volatile DatabaseConnection instance;

    /**
     * Private constructor to prevent direct instantiation (Singleton Pattern).
     */
    private DatabaseConnection() {
        loadConfiguration();
        loadDriver();
    }

    /**
     * Returns the global Singleton instance of DatabaseConnection with
     * double-checked locking.
     *
     * @return DatabaseConnection singleton instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Loads connection settings from environment variables, db.properties, or
     * constants.
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                LOGGER.info("Loaded database configuration from db.properties");
            } else {
                LOGGER.warning("db.properties not found on classpath, using defaults/environment variables.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read db.properties file, falling back to defaults.", e);
        }

        // 1. Driver Name
        this.driver = System.getenv("DB_DRIVER");
        if (this.driver == null || this.driver.trim().isEmpty()) {
            this.driver = props.getProperty("db.driver", DEFAULT_DRIVER);
        }

        // 2. Database URL
        this.url = System.getenv("DB_URL");
        if (this.url == null || this.url.trim().isEmpty()) {
            this.url = props.getProperty("db.url", DEFAULT_URL);
        }

        // 3. Database Username
        this.username = System.getenv("DB_USER");
        if (this.username == null || this.username.trim().isEmpty()) {
            this.username = props.getProperty("db.user", DEFAULT_USER);
        }

        // 4. Database Password
        this.password = System.getenv("DB_PASSWORD");
        if (this.password == null) {
            this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
        }
    }

    /**
     * Loads the MySQL JDBC Driver into the JVM.
     */
    private void loadDriver() {
        try {
            Class.forName(this.driver);
            LOGGER.info("MySQL JDBC Driver registered successfully: " + this.driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found! Ensure mysql-connector-j is included in pom.xml", e);
            throw new RuntimeException("Database driver not found: " + this.driver, e);
        }
    }

    /**
     * Establishes and returns a new active Connection to the MySQL database.
     * DAO classes must invoke this method within a try-with-resources block.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to establish connection to: " + this.url, e);
            throw e;
        }
    }

    /**
     * Convenience static method to quickly fetch a connection.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getNewConnection() throws SQLException {
        return getInstance().getConnection();
    }

    /**
     * Utility method to test database connectivity during application startup.
     *
     * @return true if database is reachable, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connectivity test failed: " + e.getMessage(), e);
            return false;
        }
    }

    // Getters for diagnostic and logging purposes
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }
}
