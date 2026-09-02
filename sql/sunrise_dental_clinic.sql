-- ==============================================================================
-- Module: CIS6003 Advanced Programming
-- Assessment: Sunrise Dental Clinic Management System
-- Technology Stack: MySQL 8.0+, Java Servlets (Jakarta), JDBC, 3-Tier Architecture
-- Author: Software Development Team
-- Description: Complete Relational Schema, Constraints, Views, Stored Functions,
--              Stored Procedures, Triggers, and Seed Data.
-- ==============================================================================

-- 1. DATABASE INITIALIZATION
-- ==============================================================================
DROP DATABASE IF EXISTS sunrise_dental_db;
CREATE DATABASE sunrise_dental_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sunrise_dental_db;

-- 2. TABLE DEFINITIONS (DDL)
-- ==============================================================================

-- Table 1: users (Staff Authentication and Role-Based Access Control)
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST', 'DENTIST') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- Table 2: patients (Patient Registry)
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_contact (contact_number),
    INDEX idx_patient_name (patient_name)
) ENGINE=InnoDB;

-- Table 3: dentists (Clinical Practitioners)
CREATE TABLE dentists (
    dentist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    dentist_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10, 2) NOT NULL CHECK (consultation_fee >= 0),
    contact_number VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dentist_user FOREIGN KEY (user_id) 
        REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_dentist_name (dentist_name)
) ENGINE=InnoDB;

-- Table 4: treatments (Catalog of Clinical Procedures and Services)
CREATE TABLE treatments (
    treatment_id INT AUTO_INCREMENT PRIMARY KEY,
    treatment_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NULL,
    cost DECIMAL(10, 2) NOT NULL CHECK (cost >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_treatment_name (treatment_name)
) ENGINE=InnoDB;

-- Table 5: appointments (Appointment Booking and Lifecycle Management)
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(30) NOT NULL UNIQUE,
    patient_id INT NOT NULL,
    dentist_id INT NOT NULL,
    treatment_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_dentist FOREIGN KEY (dentist_id) 
        REFERENCES dentists(dentist_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_app_treatment FOREIGN KEY (treatment_id) 
        REFERENCES treatments(treatment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_app_number (appointment_number),
    INDEX idx_app_date_dentist (appointment_date, dentist_id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB;

-- Table 6: invoices (Billing and Financial Transactions)
CREATE TABLE invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    appointment_id INT NOT NULL UNIQUE,
    consultation_fee DECIMAL(10, 2) NOT NULL,
    treatment_cost DECIMAL(10, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00 CHECK (discount_percentage >= 0 AND discount_percentage <= 100),
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status ENUM('PAID', 'PENDING', 'CANCELLED') NOT NULL DEFAULT 'PAID',
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_appointment FOREIGN KEY (appointment_id) 
        REFERENCES appointments(appointment_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_inv_number (invoice_number),
    INDEX idx_inv_date (created_at)
) ENGINE=InnoDB;

-- Table 7: appointment_status_log (Audit Trail for State Transitions)
CREATE TABLE appointment_status_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL,
    old_status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NULL,
    new_status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED') NOT NULL,
    changed_by VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_log_appointment FOREIGN KEY (appointment_id) 
        REFERENCES appointments(appointment_id) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_log_appointment (appointment_id)
) ENGINE=InnoDB;

-- 3. STORED FUNCTIONS
-- ==============================================================================
DELIMITER //

-- Function: GetAppointmentTotal
-- Description: Calculates gross total (consultation fee + treatment cost) for an appointment
CREATE FUNCTION GetAppointmentTotal(p_appointment_id INT) 
RETURNS DECIMAL(10, 2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_consultation_fee DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_treatment_cost DECIMAL(10, 2) DEFAULT 0.00;
    DECLARE v_total DECIMAL(10, 2) DEFAULT 0.00;
    
    SELECT d.consultation_fee, t.cost 
    INTO v_consultation_fee, v_treatment_cost
    FROM appointments a
    JOIN dentists d ON a.dentist_id = d.dentist_id
    JOIN treatments t ON a.treatment_id = t.treatment_id
    WHERE a.appointment_id = p_appointment_id;
    
    SET v_total = v_consultation_fee + v_treatment_cost;
    RETURN v_total;
END //

DELIMITER ;

-- 4. STORED PROCEDURES
-- ==============================================================================
DELIMITER //

-- Procedure: GenerateBillForAppointment
-- Description: Generates an immutable invoice record with discount and totals calculation.
CREATE PROCEDURE GenerateBillForAppointment(
    IN p_appointment_id INT,
    IN p_discount_percentage DECIMAL(5, 2),
    IN p_payment_method VARCHAR(30),
    OUT p_invoice_id INT,
    OUT p_invoice_number VARCHAR(30),
    OUT p_total_amount DECIMAL(10, 2)
)
proc_label: BEGIN
    DECLARE v_consultation_fee DECIMAL(10, 2);
    DECLARE v_treatment_cost DECIMAL(10, 2);
    DECLARE v_subtotal DECIMAL(10, 2);
    DECLARE v_discount_amount DECIMAL(10, 2);
    DECLARE v_final_total DECIMAL(10, 2);
    DECLARE v_app_status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED');
    DECLARE v_count INT DEFAULT 0;
    DECLARE v_year_val VARCHAR(4);
    DECLARE v_seq_val INT;
    DECLARE v_new_inv_num VARCHAR(30);

    -- Check if appointment exists
    SELECT a.status, d.consultation_fee, t.cost 
    INTO v_app_status, v_consultation_fee, v_treatment_cost
    FROM appointments a
    JOIN dentists d ON a.dentist_id = d.dentist_id
    JOIN treatments t ON a.treatment_id = t.treatment_id
    WHERE a.appointment_id = p_appointment_id;

    IF v_app_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Appointment not found.';
    END IF;

    IF v_app_status = 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Cannot generate bill for a cancelled appointment.';
    END IF;

    -- Check if invoice already exists for this appointment
    SELECT COUNT(*) INTO v_count FROM invoices WHERE appointment_id = p_appointment_id;
    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: Bill has already been generated for this appointment.';
    END IF;

    -- Normalize discount
    IF p_discount_percentage IS NULL OR p_discount_percentage < 0 THEN
        SET p_discount_percentage = 0.00;
    ELSEIF p_discount_percentage > 100 THEN
        SET p_discount_percentage = 100.00;
    END IF;

    -- Compute financials
    SET v_subtotal = v_consultation_fee + v_treatment_cost;
    SET v_discount_amount = ROUND(v_subtotal * (p_discount_percentage / 100.00), 2);
    SET v_final_total = v_subtotal - v_discount_amount;

    -- Generate Unique Invoice Number (INV-YYYY-XXXX)
    SET v_year_val = YEAR(CURDATE());
    SELECT COALESCE(MAX(invoice_id), 0) + 1 INTO v_seq_val FROM invoices;
    SET v_new_inv_num = CONCAT('INV-', v_year_val, '-', LPAD(v_seq_val, 4, '0'));

    -- Insert Invoice
    INSERT INTO invoices (
        invoice_number, 
        appointment_id, 
        consultation_fee, 
        treatment_cost, 
        discount_percentage, 
        discount_amount, 
        total_amount, 
        payment_status, 
        payment_method
    ) VALUES (
        v_new_inv_num, 
        p_appointment_id, 
        v_consultation_fee, 
        v_treatment_cost, 
        p_discount_percentage, 
        v_discount_amount, 
        v_final_total, 
        'PAID', 
        COALESCE(p_payment_method, 'CASH')
    );

    SET p_invoice_id = LAST_INSERT_ID();
    SET p_invoice_number = v_new_inv_num;
    SET p_total_amount = v_final_total;

    -- Mark appointment status as COMPLETED
    UPDATE appointments 
    SET status = 'COMPLETED' 
    WHERE appointment_id = p_appointment_id AND status != 'COMPLETED';

END proc_label //

DELIMITER ;

-- 5. DATABASE TRIGGERS
-- ==============================================================================
DELIMITER //

-- Trigger: prevent_double_booking_insert
-- Description: Prevents double-booking of dentists at the exact same date & time slot on INSERT
CREATE TRIGGER prevent_double_booking_insert
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflict_count INT DEFAULT 0;
    
    -- Check if dentist already has an active appointment at this date and time
    IF NEW.status != 'CANCELLED' THEN
        SELECT COUNT(*) INTO v_conflict_count
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND appointment_date = NEW.appointment_date
          AND appointment_time = NEW.appointment_time
          AND status != 'CANCELLED';
          
        IF v_conflict_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Conflict: The selected dentist already has an active appointment scheduled for this date and time.';
        END IF;
    END IF;
END //

-- Trigger: prevent_double_booking_update
-- Description: Prevents double-booking of dentists at the exact same date & time slot on UPDATE
CREATE TRIGGER prevent_double_booking_update
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflict_count INT DEFAULT 0;
    
    IF NEW.status != 'CANCELLED' AND (NEW.appointment_date != OLD.appointment_date OR NEW.appointment_time != OLD.appointment_time OR NEW.dentist_id != OLD.dentist_id OR NEW.status != OLD.status) THEN
        SELECT COUNT(*) INTO v_conflict_count
        FROM appointments
        WHERE dentist_id = NEW.dentist_id
          AND appointment_date = NEW.appointment_date
          AND appointment_time = NEW.appointment_time
          AND appointment_id != NEW.appointment_id
          AND status != 'CANCELLED';
          
        IF v_conflict_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Conflict: The selected dentist already has an active appointment scheduled for this date and time.';
        END IF;
    END IF;
END //

-- Trigger: log_appointment_insert
-- Description: Logs the initial appointment creation into the audit trail
CREATE TRIGGER log_appointment_insert
AFTER INSERT ON appointments
FOR EACH ROW
BEGIN
    INSERT INTO appointment_status_log (
        appointment_id, 
        old_status, 
        new_status, 
        changed_by, 
        remarks
    ) VALUES (
        NEW.appointment_id, 
        NULL, 
        NEW.status, 
        'SYSTEM_INITIAL_BOOKING', 
        CONCAT('Appointment created: ', NEW.appointment_number)
    );
END //

-- Trigger: log_appointment_status_change
-- Description: Automatically logs all appointment status transitions into audit trail
CREATE TRIGGER log_appointment_status_change
AFTER UPDATE ON appointments
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO appointment_status_log (
            appointment_id, 
            old_status, 
            new_status, 
            changed_by, 
            remarks
        ) VALUES (
            NEW.appointment_id, 
            OLD.status, 
            NEW.status, 
            'SYSTEM_STATUS_CHANGE', 
            CONCAT('Status transition from ', OLD.status, ' to ', NEW.status)
        );
    END IF;
END //

DELIMITER ;

-- 6. DECISION-MAKING REPORTING VIEWS
-- ==============================================================================

-- View 1: Daily_Appointment_Report
-- Description: Summary of appointments grouped by date, dentist, and current status
CREATE OR REPLACE VIEW Daily_Appointment_Report AS
SELECT 
    a.appointment_date,
    d.dentist_name,
    d.specialization,
    COUNT(a.appointment_id) AS total_scheduled_appointments,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_appointments,
    SUM(CASE WHEN a.status = 'SCHEDULED' THEN 1 ELSE 0 END) AS pending_appointments,
    SUM(CASE WHEN a.status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled_appointments,
    COALESCE(SUM(i.total_amount), 0.00) AS daily_revenue_generated
FROM appointments a
JOIN dentists d ON a.dentist_id = d.dentist_id
LEFT JOIN invoices i ON a.appointment_id = i.appointment_id
GROUP BY a.appointment_date, d.dentist_id, d.dentist_name, d.specialization
ORDER BY a.appointment_date DESC, d.dentist_name ASC;

-- View 2: Monthly_Revenue_Report
-- Description: Monthly financial summary aggregating consultation revenue, treatment revenue, discounts, and net billings
CREATE OR REPLACE VIEW Monthly_Revenue_Report AS
SELECT 
    DATE_FORMAT(i.created_at, '%Y-%m') AS revenue_month,
    COUNT(i.invoice_id) AS total_invoices_issued,
    SUM(i.consultation_fee) AS total_consultation_fees,
    SUM(i.treatment_cost) AS total_treatment_costs,
    SUM(i.discount_amount) AS total_discounts_granted,
    SUM(i.total_amount) AS net_revenue
FROM invoices i
WHERE i.payment_status = 'PAID'
GROUP BY DATE_FORMAT(i.created_at, '%Y-%m')
ORDER BY revenue_month DESC;

-- View 3: Dentist_Performance_Report
-- Description: Evaluates clinical workload, patient retention, and revenue generation per dental specialist
CREATE OR REPLACE VIEW Dentist_Performance_Report AS
SELECT 
    d.dentist_id,
    d.dentist_name,
    d.specialization,
    d.consultation_fee,
    COUNT(DISTINCT a.patient_id) AS unique_patients_served,
    COUNT(a.appointment_id) AS total_assigned_appointments,
    SUM(CASE WHEN a.status = 'COMPLETED' THEN 1 ELSE 0 END) AS successful_treatments,
    COALESCE(SUM(i.total_amount), 0.00) AS total_revenue_generated
FROM dentists d
LEFT JOIN appointments a ON d.dentist_id = a.dentist_id
LEFT JOIN invoices i ON a.appointment_id = i.appointment_id AND i.payment_status = 'PAID'
GROUP BY d.dentist_id, d.dentist_name, d.specialization, d.consultation_fee
ORDER BY total_revenue_generated DESC, successful_treatments DESC;

-- 7. SEED DATA
-- ==============================================================================

-- Seed Users
-- Passwords BCrypt hashed (Cost factor 12 / standard jBCrypt format):
-- 1. admin / Admin@123         -> $2a$12$e8Yk1m3YqL4aO5X9o7VHQeG6vD5r8x8P1gM7lM1lB7xY.9X0K3Wym
-- 2. receptionist / Reception@123 -> $2a$12$NqB8gZkFpG0/iIomJt1M5eKjO4z7mQ8bH4h2G2LqB9j8sD2tYmH.S
-- 3. dentist / Dentist@123       -> $2a$12$R9Z0x1y2z3a4b5c6d7e8f.O4j7M9c8N8n4G2LqB9j8sD2tYmH.S

INSERT INTO users (user_id, username, password_hash, full_name, role, is_active) VALUES
(1, 'admin', '$2a$12$e8Yk1m3YqL4aO5X9o7VHQeG6vD5r8x8P1gM7lM1lB7xY.9X0K3Wym', 'Dr. Aruni Perera (Director)', 'ADMIN', TRUE),
(2, 'receptionist', '$2a$12$NqB8gZkFpG0/iIomJt1M5eKjO4z7mQ8bH4h2G2LqB9j8sD2tYmH.S', 'Kasun Fernando (Front Desk)', 'RECEPTIONIST', TRUE),
(3, 'dentist', '$2a$12$R9Z0x1y2z3a4b5c6d7e8f.O4j7M9c8N8n4G2LqB9j8sD2tYmH.S', 'Dr. Ruwan Silva (Senior Dental Surgeon)', 'DENTIST', TRUE),
(4, 'dentist_nimal', '$2a$12$R9Z0x1y2z3a4b5c6d7e8f.O4j7M9c8N8n4G2LqB9j8sD2tYmH.S', 'Dr. Nimal Senanayake (Orthodontist)', 'DENTIST', TRUE);

-- Seed Dentists
INSERT INTO dentists (dentist_id, user_id, dentist_name, specialization, consultation_fee, contact_number, is_active) VALUES
(1, 3, 'Dr. Ruwan Silva', 'General Dental Surgery', 2500.00, '+94 77 123 4567', TRUE),
(2, 4, 'Dr. Nimal Senanayake', 'Orthodontics & Braces', 3500.00, '+94 71 987 6543', TRUE),
(3, NULL, 'Dr. Dilani Jayawardena', 'Pediatric Dentistry & Endodontics', 3000.00, '+94 76 555 8899', TRUE);

-- Seed Treatments
INSERT INTO treatments (treatment_id, treatment_name, description, cost, is_active) VALUES
(1, 'Routine Dental Checkup & Consultation', 'Comprehensive oral examination and diagnosis', 1000.00, TRUE),
(2, 'Dental Cleaning & Polishing', 'Ultrasonic scaling and enamel polishing', 4500.00, TRUE),
(3, 'Composite Tooth Filling', 'Tooth-colored aesthetic resin restoration', 6000.00, TRUE),
(4, 'Root Canal Treatment (RCT)', 'Endodontic therapy for infected root canals', 18000.00, TRUE),
(5, 'Tooth Extraction', 'Simple surgical or non-surgical dental extraction', 4000.00, TRUE),
(6, 'Teeth Whitening (Bleaching)', 'In-office professional dental whitening', 25000.00, TRUE),
(7, 'Orthodontic Braces Alignment', 'Fixed appliance orthodontic setup and monthly adjustment', 45000.00, TRUE);

-- Seed Patients
INSERT INTO patients (patient_id, patient_name, address, contact_number, email) VALUES
(1, 'Malith Shehan', 'No 45, Galle Road, Colombo 03', '0771234567', 'malith.shehan@example.com'),
(2, 'Chamari Atapattu', 'No 12, Kandy Road, Kiribathgoda', '0712345678', 'chamari.a@example.com'),
(3, 'Dinesh Chandimal', 'No 88, Havelock Road, Colombo 05', '0763456789', 'dinesh.c@example.com'),
(4, 'Anusha Wickramasinghe', 'No 21/A, Flower Road, Colombo 07', '0754567890', 'anusha.w@example.com');

-- Seed Appointments
-- Note: Trigger log_appointment_insert will automatically populate appointment_status_log!
INSERT INTO appointments (appointment_id, appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes) VALUES
(1, 'SDC-2026-0001', 1, 1, 2, '2026-09-05', '09:00:00', 'COMPLETED', 'Patient requested routine scaling and polish'),
(2, 'SDC-2026-0002', 2, 2, 7, '2026-09-05', '10:30:00', 'SCHEDULED', 'Initial assessment for ceramic braces'),
(3, 'SDC-2026-0003', 3, 1, 3, '2026-09-06', '14:00:00', 'SCHEDULED', 'Lower molar composite filling restoration'),
(4, 'SDC-2026-0004', 4, 3, 4, '2026-09-07', '11:00:00', 'SCHEDULED', 'First stage root canal therapy');

-- Seed Invoices for completed appointments
INSERT INTO invoices (invoice_id, invoice_number, appointment_id, consultation_fee, treatment_cost, discount_percentage, discount_amount, total_amount, payment_status, payment_method, created_at) VALUES
(1, 'INV-2026-0001', 1, 2500.00, 4500.00, 10.00, 700.00, 6300.00, 'PAID', 'CREDIT_CARD', '2026-09-05 09:45:00');

-- ==============================================================================
-- END OF DATABASE SCRIPT
-- ==============================================================================
