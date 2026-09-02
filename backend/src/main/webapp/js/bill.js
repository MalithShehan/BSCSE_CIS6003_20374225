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
        const inputEl = document.getElementById('searchAppNumInput');
        const breakdownCard = document.getElementById('billCalculationCard');
        const receiptCard = document.getElementById('printableReceiptCard');
        
        if (breakdownCard) breakdownCard.style.display = 'none';
        if (receiptCard) receiptCard.style.display = 'none';

        const valResult = Validation.validateAppointmentNumber(appNumber);
        if (!valResult.valid) {
            if (inputEl) Validation.setInvalid(inputEl, valResult.message);
            Validation.showAlert('billAlert', valResult.message, 'danger');
            return;
        } else if (inputEl) {
            Validation.setValid(inputEl);
        }

        // First check if an invoice already exists for this appointment
        const invoiceCheck = await API.get(`/bill?appointmentNumber=${encodeURIComponent(appNumber.trim().toUpperCase())}`);
        if (invoiceCheck.success && invoiceCheck.data) {
            this.renderPrintableReceipt(invoiceCheck.data);
            Validation.showAlert('billAlert', 'An official invoice has already been generated and settled for this appointment.', 'info');
            return;
        }

        // Otherwise load appointment to generate new invoice
        const appRes = await API.get(`/appointments?appointmentNumber=${encodeURIComponent(appNumber.trim().toUpperCase())}`);
        if (appRes.success && appRes.data) {
            const app = appRes.data;
            if (app.status === 'CANCELLED') {
                Validation.showAlert('billAlert', 'Cannot generate bill for a CANCELLED appointment.', 'danger');
                return;
            }

            this.activeAppointment = app;
            this.renderCalculationForm(app);
        } else {
            if (inputEl) Validation.setInvalid(inputEl, 'No appointment record found matching this reference.');
            Validation.showAlert('billAlert', appRes.message || 'Appointment record not found.', 'danger');
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
        let discountPct = 0;
        if (discountInput) {
            const val = discountInput.value;
            const discVal = Validation.validateDiscount(val);
            if (!discVal.valid) {
                Validation.setInvalid(discountInput, discVal.message);
                discountPct = 0;
            } else {
                Validation.setValid(discountInput);
                discountPct = parseFloat(val) || 0;
            }
        }

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

        const discountVal = discountInput ? discountInput.value : '0';
        const discCheck = Validation.validateDiscount(discountVal);
        if (!discCheck.valid) {
            Validation.setInvalid(discountInput, discCheck.message);
            Validation.showAlert('billAlert', discCheck.message, 'danger');
            return;
        }

        const paymentMethod = paymentMethodSelect ? paymentMethodSelect.value : 'CASH';
        const payCheck = Validation.validatePaymentMethod(paymentMethod);
        if (!payCheck.valid) {
            Validation.setInvalid(paymentMethodSelect, payCheck.message);
            Validation.showAlert('billAlert', payCheck.message, 'danger');
            return;
        }

        const discountPct = parseFloat(discountVal) || 0;

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
