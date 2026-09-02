package model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model class representing a registered Patient in the clinic.
 */
public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private int patientId;
    private String patientName;
    private String address;
    private String contactNumber;
    private String email;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Patient() {
    }

    public Patient(int patientId, String patientName, String address, String contactNumber, String email) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public Patient(String patientName, String address, String contactNumber, String email) {
        this.patientName = patientName;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", patientName='" + patientName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
