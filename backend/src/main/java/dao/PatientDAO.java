package dao;

import config.DatabaseConnection;
import model.Patient;

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
 * Data Access Object for Patient Registry operations.
 */
public class PatientDAO {

    private static final Logger LOGGER = Logger.getLogger(PatientDAO.class.getName());

    /**
     * Finds a patient by primary key ID.
     */
    public Patient findById(int patientId) throws SQLException {
        String sql = "SELECT patient_id, patient_name, address, contact_number, email, created_at, updated_at " +
                     "FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding patient by ID: " + patientId, e);
            throw e;
        }
        return null;
    }

    /**
     * Finds a patient by exact contact number.
     */
    public Patient findByContactNumber(String contactNumber) throws SQLException {
        String sql = "SELECT patient_id, patient_name, address, contact_number, email, created_at, updated_at " +
                     "FROM patients WHERE contact_number = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contactNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPatient(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding patient by contact: " + contactNumber, e);
            throw e;
        }
        return null;
    }

    /**
     * Creates a new patient record or returns existing patient if duplicate phone match is found.
     *
     * @param patient Patient to insert
     * @return Generated or existing patient_id
     */
    public int createPatient(Patient patient) throws SQLException {
        // Check if patient already exists with same contact number
        Patient existing = findByContactNumber(patient.getContactNumber());
        if (existing != null) {
            // Update address/email/name if changed
            existing.setPatientName(patient.getPatientName());
            existing.setAddress(patient.getAddress());
            if (patient.getEmail() != null) {
                existing.setEmail(patient.getEmail());
            }
            updatePatient(existing);
            return existing.getPatientId();
        }

        String sql = "INSERT INTO patients (patient_name, address, contact_number, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, patient.getPatientName());
            stmt.setString(2, patient.getAddress());
            stmt.setString(3, patient.getContactNumber());
            stmt.setString(4, patient.getEmail());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating patient failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    patient.setPatientId(generatedKeys.getInt(1));
                    return patient.getPatientId();
                } else {
                    throw new SQLException("Creating patient failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating patient: " + patient.getPatientName(), e);
            throw e;
        }
    }

    /**
     * Updates patient details.
     */
    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET patient_name = ?, address = ?, contact_number = ?, email = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patient.getPatientName());
            stmt.setString(2, patient.getAddress());
            stmt.setString(3, patient.getContactNumber());
            stmt.setString(4, patient.getEmail());
            stmt.setInt(5, patient.getPatientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating patient: " + patient.getPatientId(), e);
            throw e;
        }
    }

    /**
     * Searches patients by name, phone number, or address keyword.
     */
    public List<Patient> searchPatients(String keyword) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT patient_id, patient_name, address, contact_number, email, created_at, updated_at " +
                     "FROM patients WHERE patient_name LIKE ? OR contact_number LIKE ? ORDER BY patient_name ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            stmt.setString(1, term);
            stmt.setString(2, term);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching patients with keyword: " + keyword, e);
            throw e;
        }
        return patients;
    }

    /**
     * Retrieves all registered patients.
     */
    public List<Patient> findAll() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT patient_id, patient_name, address, contact_number, email, created_at, updated_at " +
                     "FROM patients ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all patients", e);
            throw e;
        }
        return list;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientName(rs.getString("patient_name"));
        p.setAddress(rs.getString("address"));
        p.setContactNumber(rs.getString("contact_number"));
        p.setEmail(rs.getString("email"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    }
}
