package dao;

import config.DatabaseConnection;
import model.Treatment;

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
 * Data Access Object for Treatment procedure catalog.
 */
public class TreatmentDAO {

    private static final Logger LOGGER = Logger.getLogger(TreatmentDAO.class.getName());

    /**
     * Retrieves all active treatments.
     */
    public List<Treatment> findAllActive() throws SQLException {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT treatment_id, treatment_name, description, cost, is_active, created_at " +
                     "FROM treatments WHERE is_active = TRUE ORDER BY treatment_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTreatment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving active treatments", e);
            throw e;
        }
        return list;
    }

    /**
     * Retrieves all treatments.
     */
    public List<Treatment> findAll() throws SQLException {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT treatment_id, treatment_name, description, cost, is_active, created_at " +
                     "FROM treatments ORDER BY treatment_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToTreatment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving treatments", e);
            throw e;
        }
        return list;
    }

    /**
     * Finds a treatment by ID.
     */
    public Treatment findById(int treatmentId) throws SQLException {
        String sql = "SELECT treatment_id, treatment_name, description, cost, is_active, created_at " +
                     "FROM treatments WHERE treatment_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, treatmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTreatment(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding treatment by ID: " + treatmentId, e);
            throw e;
        }
        return null;
    }

    /**
     * Creates a new Treatment item.
     */
    public int createTreatment(Treatment treatment) throws SQLException {
        String sql = "INSERT INTO treatments (treatment_name, description, cost, is_active) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, treatment.getTreatmentName());
            stmt.setString(2, treatment.getDescription());
            stmt.setBigDecimal(3, treatment.getCost());
            stmt.setBoolean(4, treatment.isActive());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating treatment failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    treatment.setTreatmentId(generatedKeys.getInt(1));
                    return treatment.getTreatmentId();
                } else {
                    throw new SQLException("Creating treatment failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating treatment: " + treatment.getTreatmentName(), e);
            throw e;
        }
    }

    private Treatment mapResultSetToTreatment(ResultSet rs) throws SQLException {
        Treatment t = new Treatment();
        t.setTreatmentId(rs.getInt("treatment_id"));
        t.setTreatmentName(rs.getString("treatment_name"));
        t.setDescription(rs.getString("description"));
        t.setCost(rs.getBigDecimal("cost"));
        t.setActive(rs.getBoolean("is_active"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
