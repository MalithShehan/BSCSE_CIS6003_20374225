package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model class representing a Clinical Treatment / Procedure in the catalog.
 */
public class Treatment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int treatmentId;
    private String treatmentName;
    private String description;
    private BigDecimal cost;
    private boolean active;
    private Timestamp createdAt;

    public Treatment() {
    }

    public Treatment(int treatmentId, String treatmentName, String description, BigDecimal cost, boolean active) {
        this.treatmentId = treatmentId;
        this.treatmentName = treatmentName;
        this.description = description;
        this.cost = cost;
        this.active = active;
    }

    public Treatment(String treatmentName, String description, BigDecimal cost) {
        this.treatmentName = treatmentName;
        this.description = description;
        this.cost = cost;
        this.active = true;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
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
        return "Treatment{" +
                "treatmentId=" + treatmentId +
                ", treatmentName='" + treatmentName + '\'' +
                ", cost=" + cost +
                '}';
    }
}
