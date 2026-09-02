package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a Dental Specialist / Practitioner.
 */
public class Dentist implements Serializable {
    private static final long serialVersionUID = 1L;

    private int dentistId;
    private Integer userId; // Optional link to User account
    private String dentistName;
    private String specialization;
    private BigDecimal consultationFee;
    private String contactNumber;
    private boolean active;
    private Timestamp createdAt;

    public Dentist() {
    }

    public Dentist(int dentistId, Integer userId, String dentistName, String specialization, BigDecimal consultationFee, String contactNumber, boolean active) {
        this.dentistId = dentistId;
        this.userId = userId;
        this.dentistName = dentistName;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.contactNumber = contactNumber;
        this.active = active;
    }

    public Dentist(String dentistName, String specialization, BigDecimal consultationFee, String contactNumber) {
        this.dentistName = dentistName;
        this.specialization = specialization;
        this.consultationFee = consultationFee;
        this.contactNumber = contactNumber;
        this.active = true;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Dentist{" +
                "dentistId=" + dentistId +
                ", dentistName='" + dentistName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", consultationFee=" + consultationFee +
                '}';
    }
}
