package service;

import dao.AppointmentDAO;
import dao.DentistDAO;
import dao.PatientDAO;
import dao.TreatmentDAO;
import model.Appointment;
import model.Dentist;
import model.Patient;
import model.Treatment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit & Mock Tests")
public class AppointmentServiceTest {

    @Mock
    private AppointmentDAO appointmentDAO;
    @Mock
    private PatientDAO patientDAO;
    @Mock
    private DentistDAO dentistDAO;
    @Mock
    private TreatmentDAO treatmentDAO;
    @Mock
    private NotificationService emailNotifier;
    @Mock
    private NotificationService smsNotifier;

    @InjectMocks
    private AppointmentService appointmentService;

    private Dentist mockDentist;
    private Treatment mockTreatment;

    @BeforeEach
    void setUp() {
        mockDentist = new Dentist(1, 3, "Dr. Ruwan Silva", "General Dentistry", new BigDecimal("2500.00"), "0771234567", true);
        mockTreatment = new Treatment(1, "Cleaning & Polishing", "Ultrasonic scaling", new BigDecimal("4500.00"), true);
    }

    @Test
    @DisplayName("Should successfully book appointment and return SDC-YYYY-XXXX number")
    void testBookAppointment_Success() throws SQLException {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(2));
        Time clinicTime = Time.valueOf("10:00:00");

        when(dentistDAO.findById(1)).thenReturn(mockDentist);
        when(treatmentDAO.findById(1)).thenReturn(mockTreatment);
        when(appointmentDAO.isDoubleBooked(eq(1), eq(futureDate), eq(clinicTime), isNull())).thenReturn(false);
        when(patientDAO.createPatient(any(Patient.class))).thenReturn(10);
        when(appointmentDAO.getNextSequenceNumber(anyInt())).thenReturn(1);
        when(appointmentDAO.createAppointment(any(Appointment.class))).thenReturn(100);

        Appointment result = appointmentService.bookAppointment(
                "Kasun Dias", "No 12, Highlevel Rd, Nugegoda", "0779998888", "kasun@example.com",
                1, 1, futureDate, clinicTime, "Routine checkup"
        );

        assertNotNull(result);
        assertEquals("SDC-2026-0001", result.getAppointmentNumber());
        assertEquals("Kasun Dias", result.getPatientName());
        assertEquals(new BigDecimal("7000.00"), result.getTotalEstimatedCost()); // 2500 + 4500
        verify(appointmentDAO, times(1)).createAppointment(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when dentist is already double-booked")
    void testBookAppointment_DoubleBookingConflict() throws SQLException {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(1));
        Time clinicTime = Time.valueOf("09:00:00");

        when(dentistDAO.findById(1)).thenReturn(mockDentist);
        when(treatmentDAO.findById(1)).thenReturn(mockTreatment);
        when(appointmentDAO.isDoubleBooked(eq(1), eq(futureDate), eq(clinicTime), isNull())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            appointmentService.bookAppointment(
                    "Malith Shehan", "Colombo", "0771234567", "malith@example.com",
                    1, 1, futureDate, clinicTime, null
            )
        );

        assertTrue(ex.getMessage().contains("Conflict"));
        verify(appointmentDAO, never()).createAppointment(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when date is in the past")
    void testBookAppointment_PastDate() throws SQLException {
        Date pastDate = Date.valueOf(LocalDate.now().minusDays(5));
        Time clinicTime = Time.valueOf("11:00:00");

        when(dentistDAO.findById(1)).thenReturn(mockDentist);
        when(treatmentDAO.findById(1)).thenReturn(mockTreatment);

        assertThrows(IllegalArgumentException.class, () ->
            appointmentService.bookAppointment(
                    "Malith Shehan", "Colombo", "0771234567", "malith@example.com",
                    1, 1, pastDate, clinicTime, null
            )
        );
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when time is outside clinic hours")
    void testBookAppointment_OutsideOperatingHours() throws SQLException {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(1));
        Time eveningTime = Time.valueOf("19:00:00"); // 7 PM (after 17:00 close)

        when(dentistDAO.findById(1)).thenReturn(mockDentist);
        when(treatmentDAO.findById(1)).thenReturn(mockTreatment);

        assertThrows(IllegalArgumentException.class, () ->
            appointmentService.bookAppointment(
                    "Malith Shehan", "Colombo", "0771234567", "malith@example.com",
                    1, 1, futureDate, eveningTime, null
            )
        );
    }

    @Test
    @DisplayName("Should fetch appointment by valid reference number")
    void testGetAppointmentByNumber_Success() throws SQLException {
        Appointment sampleApp = new Appointment();
        sampleApp.setAppointmentNumber("SDC-2026-0001");
        sampleApp.setPatientName("Chamari Atapattu");

        when(appointmentDAO.findByAppointmentNumber("SDC-2026-0001")).thenReturn(sampleApp);

        Appointment found = appointmentService.getAppointmentByNumber("SDC-2026-0001");
        assertNotNull(found);
        assertEquals("Chamari Atapattu", found.getPatientName());
    }
}
