/**
 * Billing and Printable Receipt Script.
 */
const BillingManager = {
    activeAppointment: null,

    /**
     * Initializes billing page from query parameters if available.
     */
    async init() {
        const urlParams = new URLSearchParams(window.location.search);
        const appNumber = urlParams.get('appointmentNumber');
        if (appNumber) {
            const input = document.getElementById('searchAppNumInput');
            if (input) input.value = appNumber;
            await this.loadAppointmentForBilling(appNumber);
        }
    },

    /**
     * Searches appointment and loads cost breakdown.
     */
    async loadAppointmentForBilling(appNumber) {
        Validation.clearAlert('billAlert');
        const breakdownCard = document.getElementById('billCalculationCard');
        const receiptCard = document.getElementById('printableReceiptCard');
        
        if (breakdownCard) breakdownCard.style.display = 'none';
        if (receiptCard) receiptCard.style.display = 'none';

        if (!Validation.isValidAppointmentNumber(appNumber)) {
            Validation.showAlert('billAlert', 'Please enter a valid appointment number (e.g., SDC-2026-0001).', 'danger');
            return;
        }

        // First check if an invoice already exists for this appointment
        const invoiceCheck = await API.get(`/bill?appointmentNumber=${encodeURIComponent(appNumber.trim())}`);
        if (invoiceCheck.success && invoiceCheck.data) {
            this.renderPrintableReceipt(invoiceCheck.data);
            Validation.showAlert('billAlert', 'An official invoice has already been generated for this appointment.', 'info');
            return;
        }

        // Otherwise load appointment to generate new invoice
        const appRes = await API.get(`/appointments?appointmentNumber=${encodeURIComponent(appNumber.trim())}`);
        if (appRes.success && appRes.data) {
            const app = appRes.data;
            if (app.status === 'CANCELLED') {
                Validation.showAlert('billAlert', 'Cannot generate bill for a CANCELLED appointment.', 'danger');
                return;
            }

            this.activeAppointment = app;
            this.renderCalculationForm(app);
        } else {
            Validation.showAlert('billAlert', appRes.message || 'Appointment not found.', 'danger');
        }
    },

    /**
     * Renders calculation form and updates live math calculations.
     */
    renderCalculationForm(app) {
        const card = document.getElementById('billCalculationCard');
        if (!card) return;

        document.getElementById('calcAppNumber').textContent = app.appointmentNumber;
        document.getElementById('calcPatient').textContent = `${app.patientName} (${app.patientContact})`;
        document.getElementById('calcDentist').textContent = `${app.dentistName} (${app.dentistSpecialization})`;
        document.getElementById('calcTreatment').textContent = app.treatmentName;

        document.getElementById('calcDentistFee').textContent = `LKR ${parseFloat(app.dentistFee).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('calcTreatmentCost').textContent = `LKR ${parseFloat(app.treatmentCost).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;

        this.updateTotals();
        card.style.display = 'block';
    },

    /**
     * Calculates discount and final net total dynamically.
     */
    updateTotals() {
        if (!this.activeAppointment) return;

        const dentistFee = parseFloat(this.activeAppointment.dentistFee || 0);
        const treatmentCost = parseFloat(this.activeAppointment.treatmentCost || 0);
        const subtotal = dentistFee + treatmentCost;

        const discountInput = document.getElementById('discountInput');
        let discountPct = discountInput ? parseFloat(discountInput.value) || 0 : 0;
        if (discountPct < 0) discountPct = 0;
        if (discountPct > 100) discountPct = 100;

        const discountAmount = subtotal * (discountPct / 100.0);
        const netTotal = subtotal - discountAmount;

        document.getElementById('calcSubtotal').textContent = `LKR ${subtotal.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('calcDiscountAmount').textContent = `- LKR ${discountAmount.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('calcNetTotal').textContent = `LKR ${netTotal.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
    },

    /**
     * Invokes the Stored Procedure via API to officially generate the invoice.
     */
    async generateBill() {
        if (!this.activeAppointment) return;
        Validation.clearAlert('billAlert');

        const discountInput = document.getElementById('discountInput');
        const paymentMethodSelect = document.getElementById('paymentMethodSelect');

        const discountPct = discountInput ? parseFloat(discountInput.value) || 0 : 0;
        const paymentMethod = paymentMethodSelect ? paymentMethodSelect.value : 'CASH';

        const btn = document.getElementById('btnConfirmBill');
        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Generating Official Invoice...';
        }

        const payload = {
            appointmentId: this.activeAppointment.appointmentId,
            discountPercentage: discountPct,
            paymentMethod: paymentMethod
        };

        const response = await API.post('/bill', payload);

        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Issue Official Invoice & Settlement';
        }

        if (response.success && response.data) {
            const invoice = response.data;
            document.getElementById('billCalculationCard').style.display = 'none';
            this.renderPrintableReceipt(invoice);
            Validation.showAlert('billAlert', `Invoice ${invoice.invoiceNumber} generated and settled successfully!`, 'success');
        } else {
            Validation.showAlert('billAlert', response.message || 'Failed to generate invoice.', 'danger');
        }
    },

    /**
     * Renders printable invoice receipt layout.
     */
    renderPrintableReceipt(inv) {
        const receiptCard = document.getElementById('printableReceiptCard');
        if (!receiptCard) return;

        document.getElementById('recInvoiceNumber').textContent = inv.invoiceNumber;
        document.getElementById('recCreatedDate').textContent = inv.createdAt || new Date().toLocaleString();
        document.getElementById('recAppNumber').textContent = inv.appointmentNumber;
        document.getElementById('recPatientName').textContent = inv.patientName;
        document.getElementById('recPatientContact').textContent = inv.patientContact;
        document.getElementById('recPatientAddress').textContent = inv.patientAddress || 'Colombo, Sri Lanka';
        document.getElementById('recDentist').textContent = inv.dentistName;
        document.getElementById('recTreatment').textContent = inv.treatmentName;

        document.getElementById('recDentistFee').textContent = `LKR ${parseFloat(inv.consultationFee).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('recTreatmentCost').textContent = `LKR ${parseFloat(inv.treatmentCost).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        
        const subtotal = parseFloat(inv.consultationFee) + parseFloat(inv.treatmentCost);
        document.getElementById('recSubtotal').textContent = `LKR ${subtotal.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('recDiscount').textContent = `- LKR ${parseFloat(inv.discountAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })} (${inv.discountPercentage}%)`;
        document.getElementById('recGrandTotal').textContent = `LKR ${parseFloat(inv.totalAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
        document.getElementById('recPaymentMethod').textContent = inv.paymentMethod || 'CASH';

        receiptCard.style.display = 'block';
        receiptCard.scrollIntoView({ behavior: 'smooth' });
    },

    /**
     * Triggers the browser print dialog.
     */
    printReceipt() {
        window.print();
    }
};
