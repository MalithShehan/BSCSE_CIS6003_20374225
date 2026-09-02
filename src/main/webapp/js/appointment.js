/**
 * Appointment Operations Client Script.
 */
const AppointmentManager = {
    dentists: [],
    treatments: [],

    /**
     * Initializes Dentist and Treatment dropdowns.
     */
    async loadFormData() {
        try {
            const [dentistRes, treatmentRes] = await Promise.all([
                API.get('/dentists'),
                API.get('/treatments')
            ]);

            if (dentistRes.success) {
                this.dentists = dentistRes.data;
                this.populateDentistSelect('dentistId');
            }

            if (treatmentRes.success) {
                this.treatments = treatmentRes.data;
                this.populateTreatmentSelect('treatmentId');
            }
        } catch (error) {
            console.error('Failed to load form catalogs:', error);
        }
    },

    populateDentistSelect(selectId) {
        const select = document.getElementById(selectId);
        if (!select) return;

        select.innerHTML = '<option value="">-- Select Dental Specialist --</option>';
        this.dentists.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.dentistId;
            opt.textContent = `${d.dentistName} (${d.specialization}) - LKR ${parseFloat(d.consultationFee).toLocaleString()}`;
            opt.dataset.fee = d.consultationFee;
            select.appendChild(opt);
        });
    },

    populateTreatmentSelect(selectId) {
        const select = document.getElementById(selectId);
        if (!select) return;

        select.innerHTML = '<option value="">-- Select Treatment Procedure --</option>';
        this.treatments.forEach(t => {
            const opt = document.createElement('option');
            opt.value = t.treatmentId;
            opt.textContent = `${t.treatmentName} - LKR ${parseFloat(t.cost).toLocaleString()}`;
            opt.dataset.cost = t.cost;
            select.appendChild(opt);
        });
    },

    /**
     * Updates the dynamic live fee calculation box on the booking form.
     */
    updateCostPreview() {
        const dentistSelect = document.getElementById('dentistId');
        const treatmentSelect = document.getElementById('treatmentId');
        const previewElement = document.getElementById('estimatedCostDisplay');

        if (!previewElement) return;

        let consultationFee = 0;
        let treatmentCost = 0;

        if (dentistSelect && dentistSelect.selectedIndex > 0) {
            consultationFee = parseFloat(dentistSelect.options[dentistSelect.selectedIndex].dataset.fee || 0);
        }
        if (treatmentSelect && treatmentSelect.selectedIndex > 0) {
            treatmentCost = parseFloat(treatmentSelect.options[treatmentSelect.selectedIndex].dataset.cost || 0);
        }

        const total = consultationFee + treatmentCost;
        previewElement.textContent = `LKR ${total.toLocaleString('en-US', { minimumFractionDigits: 2 })}`;
    },

    /**
     * Submits the appointment booking form.
     */
    async handleBookingSubmit(event) {
        event.preventDefault();
        Validation.clearAlert('alertContainer');

        const patientName = document.getElementById('patientName').value.trim();
        const patientAddress = document.getElementById('patientAddress').value.trim();
        const patientContact = document.getElementById('patientContact').value.trim();
        const patientEmail = document.getElementById('patientEmail').value.trim();
        const dentistId = parseInt(document.getElementById('dentistId').value, 10);
        const treatmentId = parseInt(document.getElementById('treatmentId').value, 10);
        const appointmentDate = document.getElementById('appointmentDate').value;
        const appointmentTime = document.getElementById('appointmentTime').value;
        const notes = document.getElementById('notes') ? document.getElementById('notes').value.trim() : '';

        // Client-side validations
        if (!patientName) {
            Validation.showAlert('alertContainer', 'Patient name is required.', 'danger');
            return;
        }
        if (!Validation.isValidPhone(patientContact)) {
            Validation.showAlert('alertContainer', 'Please enter a valid contact number (e.g., 0771234567 or +94771234567).', 'danger');
            return;
        }
        if (patientEmail && !Validation.EMAIL_REGEX.test(patientEmail)) {
            Validation.showAlert('alertContainer', 'Please enter a valid email address.', 'danger');
            return;
        }
        if (!dentistId) {
            Validation.showAlert('alertContainer', 'Please select a dentist.', 'danger');
            return;
        }
        if (!treatmentId) {
            Validation.showAlert('alertContainer', 'Please select a treatment.', 'danger');
            return;
        }
        if (!Validation.isFutureOrToday(appointmentDate)) {
            Validation.showAlert('alertContainer', 'Appointment date cannot be in the past.', 'danger');
            return;
        }
        if (!Validation.isWithinClinicHours(appointmentTime)) {
            Validation.showAlert('alertContainer', 'Appointment time must be between 08:00 and 17:00.', 'danger');
            return;
        }

        const submitBtn = document.getElementById('btnSubmitBooking');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Scheduling Appointment...';
        }

        const payload = {
            patientName,
            patientAddress,
            patientContact,
            patientEmail,
            dentistId,
            treatmentId,
            appointmentDate,
            appointmentTime,
            notes
        };

        const response = await API.post('/appointments', payload);

        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'Confirm & Schedule Appointment';
        }

        if (response.success && response.data) {
            const app = response.data;
            Validation.showAlert('alertContainer', `
                <strong>Appointment Scheduled Successfully!</strong><br>
                Reference Number: <strong>${app.appointmentNumber}</strong><br>
                Date & Time: ${app.appointmentDate} at ${app.appointmentTime}<br>
                Specialist: ${app.dentistName} | Treatment: ${app.treatmentName}<br>
                Estimated Total: LKR ${parseFloat(app.totalEstimatedCost).toLocaleString()}
            `, 'success');

            document.getElementById('bookingForm').reset();
            this.updateCostPreview();
        } else {
            Validation.showAlert('alertContainer', response.message || 'Failed to schedule appointment.', 'danger');
        }
    },

    /**
     * Searches for a single appointment by reference number.
     */
    async searchAppointment(appNumber) {
        Validation.clearAlert('searchAlert');
        const container = document.getElementById('searchResultCard');
        if (container) container.style.display = 'none';

        if (!Validation.isValidAppointmentNumber(appNumber)) {
            Validation.showAlert('searchAlert', 'Invalid format. Expected: SDC-YYYY-XXXX (e.g. SDC-2026-0001).', 'danger');
            return;
        }

        const res = await API.get(`/appointments?appointmentNumber=${encodeURIComponent(appNumber.trim())}`);
        if (res.success && res.data) {
            this.renderAppointmentDetails(res.data);
        } else {
            Validation.showAlert('searchAlert', res.message || 'No appointment found matching reference.', 'danger');
        }
    },

    /**
     * Renders detailed appointment card.
     */
    renderAppointmentDetails(app) {
        const container = document.getElementById('searchResultCard');
        if (!container) return;

        document.getElementById('resAppNumber').textContent = app.appointmentNumber;
        document.getElementById('resPatientName').textContent = app.patientName;
        document.getElementById('resPatientContact').textContent = app.patientContact;
        document.getElementById('resPatientAddress').textContent = app.patientAddress || 'N/A';
        document.getElementById('resPatientEmail').textContent = app.patientEmail || 'N/A';
        document.getElementById('resDentist').textContent = `${app.dentistName} (${app.dentistSpecialization})`;
        document.getElementById('resTreatment').textContent = app.treatmentName;
        document.getElementById('resDateTime').textContent = `${app.appointmentDate} at ${app.appointmentTime}`;
        document.getElementById('resEstimatedCost').textContent = `LKR ${parseFloat(app.totalEstimatedCost).toLocaleString()}`;
        
        // Status Badge
        const statusEl = document.getElementById('resStatusBadge');
        statusEl.className = `badge badge-${app.status.toLowerCase()}`;
        statusEl.textContent = app.status;

        // Action Buttons Setup
        const btnBill = document.getElementById('btnGoToBill');
        const btnCancel = document.getElementById('btnCancelApp');

        if (btnBill) {
            if (app.status === 'CANCELLED') {
                btnBill.style.display = 'none';
            } else {
                btnBill.style.display = 'inline-flex';
                btnBill.onclick = () => {
                    window.location.href = `bill.html?appointmentNumber=${app.appointmentNumber}&id=${app.appointmentId}`;
                };
            }
        }

        if (btnCancel) {
            if (app.status === 'SCHEDULED') {
                btnCancel.style.display = 'inline-flex';
                btnCancel.onclick = () => this.cancelAppointment(app.appointmentId, app.appointmentNumber);
            } else {
                btnCancel.style.display = 'none';
            }
        }

        container.style.display = 'block';
    },

    /**
     * Cancels an appointment.
     */
    async cancelAppointment(id, number) {
        if (!confirm(`Are you sure you want to cancel appointment ${number}?`)) {
            return;
        }

        const res = await API.delete(`/appointments?id=${id}`);
        if (res.success) {
            alert(`Appointment ${number} has been cancelled.`);
            this.searchAppointment(number);
        } else {
            alert(res.message || 'Failed to cancel appointment.');
        }
    },

    /**
     * Loads list of appointments into a table.
     */
    async loadAppointmentTable(tableBodyId, statusFilter = 'ALL') {
        const tbody = document.getElementById(tableBodyId);
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading appointments...</td></tr>';

        const res = await API.get(`/appointments?status=${statusFilter}`);
        if (res.success && res.data && res.data.length > 0) {
            tbody.innerHTML = '';
            res.data.forEach(app => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${app.appointmentNumber}</strong></td>
                    <td>
                        <div style="font-weight:600;">${app.patientName}</div>
                        <small style="color:#64748b;">${app.patientContact}</small>
                    </td>
                    <td>${app.dentistName}</td>
                    <td>${app.treatmentName}</td>
                    <td>${app.appointmentDate} <br><small style="color:#64748b;">${app.appointmentTime}</small></td>
                    <td><span class="badge badge-${app.status.toLowerCase()}">${app.status}</span></td>
                    <td>
                        <button class="btn btn-secondary btn-sm" onclick="window.location.href='search-appointment.html?search=${app.appointmentNumber}'">View</button>
                        ${app.status !== 'CANCELLED' ? `<button class="btn btn-primary btn-sm" onclick="window.location.href='bill.html?appointmentNumber=${app.appointmentNumber}&id=${app.appointmentId}'">Bill</button>` : ''}
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748b;">No appointment records found.</td></tr>';
        }
    }
};
