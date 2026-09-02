package dao;

import config.DatabaseConnection;
import model.Dentist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Dentist practitioners.
 */
public class DentistDAO {

    private static final Logger LOGGER = Logger.getLogger(DentistDAO.class.getName());

    /**
     * Retrieves all active dentists.
     */
    public List<Dentist> findAllActive() throws SQLException {
        List<Dentist> list = new ArrayList<>();
        String sql = "SELECT dentist_id, user_id, dentist_name, specialization, consultation_fee, contact_number, is_active, created_at " +
                     "FROM dentists WHERE is_active = TRUE ORDER BY dentist_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDentist(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving active dentists", e);
            throw e;
        }
        return list;
    }

    /**
     * Retrieves all dentists (active & inactive).
     */
    public List<Dentist> findAll() throws SQLException {
        List<Dentist> list = new ArrayList<>();
        String sql = "SELECT dentist_id, user_id, dentist_name, specialization, consultation_fee, contact_number, is_active, created_at " +
                     "FROM dentists ORDER BY dentist_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToDentist(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all dentists", e);
            throw e;
        }
        return list;
    }

    /**
     * Finds a dentist by primary key ID.
     */
    public Dentist findById(int dentistId) throws SQLException {
        String sql = "SELECT dentist_id, user_id, dentist_name, specialization, consultation_fee, contact_number, is_active, created_at " +
                     "FROM dentists WHERE dentist_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDentist(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dentist by ID: " + dentistId, e);
            throw e;
        }
        return null;
    }

    /**
     * Finds a dentist linked to a system user account.
     */
    public Dentist findByUserId(int userId) throws SQLException {
        String sql = "SELECT dentist_id, user_id, dentist_name, specialization, consultation_fee, contact_number, is_active, created_at " +
                     "FROM dentists WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDentist(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dentist by user ID: " + userId, e);
            throw e;
        }
        return null;
    }

    /**
     * Creates a new Dentist profile.
     */
    public int createDentist(Dentist dentist) throws SQLException {
        String sql = "INSERT INTO dentists (user_id, dentist_name, specialization, consultation_fee, contact_number, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (dentist.getUserId() != null) {
                stmt.setInt(1, dentist.getUserId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, dentist.getDentistName());
            stmt.setString(3, dentist.getSpecialization());
            stmt.setBigDecimal(4, dentist.getConsultationFee());
            stmt.setString(5, dentist.getContactNumber());
            stmt.setBoolean(6, dentist.isActive());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating dentist failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    dentist.setDentistId(generatedKeys.getInt(1));
                    return dentist.getDentistId();
                } else {
                    throw new SQLException("Creating dentist failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating dentist: " + dentist.getDentistName(), e);
            throw e;
        }
    }

    private Dentist mapResultSetToDentist(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setDentistId(rs.getInt("dentist_id"));
        int uid = rs.getInt("user_id");
        if (!rs.wasNull()) {
            d.setUserId(uid);
        }
        d.setDentistName(rs.getString("dentist_name"));
        d.setSpecialization(rs.getString("specialization"));
        d.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        d.setContactNumber(rs.getString("contact_number"));
        d.setActive(rs.getBoolean("is_active"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        return d;
    }
}
