package service;

import dao.AppointmentDAO;
import dao.InvoiceDAO;
import model.Appointment;
import model.Invoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingService Unit & Mock Tests")
public class BillingServiceTest {

    @Mock
    private InvoiceDAO invoiceDAO;
    @Mock
    private AppointmentDAO appointmentDAO;
    @Mock
    private NotificationService smsNotifier;

    @InjectMocks
    private BillingService billingService;

    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        mockAppointment = new Appointment();
        mockAppointment.setAppointmentId(1);
        mockAppointment.setAppointmentNumber("SDC-2026-0001");
        mockAppointment.setPatientName("Malith Shehan");
        mockAppointment.setPatientContact("0771234567");
        mockAppointment.setStatus("SCHEDULED");
        mockAppointment.setDentistFee(new BigDecimal("2500.00"));
        mockAppointment.setTreatmentCost(new BigDecimal("4500.00"));
    }

    @Test
    @DisplayName("Should successfully generate invoice via stored procedure call")
    void testGenerateBill_Success() throws SQLException {
        Invoice generatedMock = new Invoice(1, "INV-2026-0001", 1, new BigDecimal("2500.00"),
                new BigDecimal("4500.00"), new BigDecimal("10.00"), new BigDecimal("700.00"),
                new BigDecimal("6300.00"), "PAID", "CASH");

        when(appointmentDAO.findById(1)).thenReturn(mockAppointment);
        when(invoiceDAO.findByAppointmentId(1)).thenReturn(null); // Not already billed
        when(invoiceDAO.generateBill(eq(1), any(BigDecimal.class), eq("CASH"))).thenReturn(generatedMock);

        Invoice result = billingService.generateBill(1, new BigDecimal("10.00"), "CASH");

        assertNotNull(result);
        assertEquals("INV-2026-0001", result.getInvoiceNumber());
        assertEquals(new BigDecimal("6300.00"), result.getTotalAmount());
        verify(invoiceDAO, times(1)).generateBill(1, new BigDecimal("10.00"), "CASH");
    }

    @Test
    @DisplayName("Should block duplicate billing if invoice already exists (Idempotency)")
    void testGenerateBill_PreventDuplicateBilling() throws SQLException {
        Invoice existingInvoice = new Invoice();
        existingInvoice.setInvoiceNumber("INV-2026-0001");

        when(appointmentDAO.findById(1)).thenReturn(mockAppointment);
        when(invoiceDAO.findByAppointmentId(1)).thenReturn(existingInvoice); // Already billed

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            billingService.generateBill(1, BigDecimal.ZERO, "CASH")
        );

        assertTrue(ex.getMessage().contains("already been generated"));
        verify(invoiceDAO, never()).generateBill(anyInt(), any(), any());
    }

    @Test
    @DisplayName("Should block billing for CANCELLED appointments")
    void testGenerateBill_CancelledAppointment() throws SQLException {
        mockAppointment.setStatus("CANCELLED");
        when(appointmentDAO.findById(1)).thenReturn(mockAppointment);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            billingService.generateBill(1, BigDecimal.ZERO, "CASH")
        );

        assertTrue(ex.getMessage().contains("cancelled"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when discount exceeds 100%")
    void testGenerateBill_InvalidDiscount() throws SQLException {
        when(appointmentDAO.findById(1)).thenReturn(mockAppointment);
        when(invoiceDAO.findByAppointmentId(1)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            billingService.generateBill(1, new BigDecimal("150.00"), "CASH")
        );
    }
}
