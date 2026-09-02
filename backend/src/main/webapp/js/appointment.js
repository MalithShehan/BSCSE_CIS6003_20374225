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
     * Submits the appointment booking form with complete field-level validations.
     */
    async handleBookingSubmit(event) {
        event.preventDefault();
        Validation.clearAlert('alertContainer');

        const nameEl = document.getElementById('patientName');
        const contactEl = document.getElementById('patientContact');
        const addressEl = document.getElementById('patientAddress');
        const emailEl = document.getElementById('patientEmail');
        const dentistEl = document.getElementById('dentistId');
        const treatmentEl = document.getElementById('treatmentId');
        const dateEl = document.getElementById('appointmentDate');
        const timeEl = document.getElementById('appointmentTime');
        const notesEl = document.getElementById('notes');

        const patientName = nameEl ? nameEl.value.trim() : '';
        const patientContact = contactEl ? contactEl.value.trim() : '';
        const patientAddress = addressEl ? addressEl.value.trim() : '';
        const patientEmail = emailEl ? emailEl.value.trim() : '';
        const dentistId = dentistEl ? parseInt(dentistEl.value, 10) : 0;
        const treatmentId = treatmentEl ? parseInt(treatmentEl.value, 10) : 0;
        const appointmentDate = dateEl ? dateEl.value : '';
        const appointmentTime = timeEl ? timeEl.value : '';
        const notes = notesEl ? notesEl.value.trim() : '';

        let isValid = true;

        // 1. Patient Name Validation
        const nameVal = Validation.validatePatientName(patientName);
        if (!nameVal.valid) {
            Validation.setInvalid(nameEl, nameVal.message);
            isValid = false;
        } else {
            Validation.setValid(nameEl);
        }

        // 2. Contact Number Validation
        const contactVal = Validation.validatePhone(patientContact);
        if (!contactVal.valid) {
            Validation.setInvalid(contactEl, contactVal.message);
            isValid = false;
        } else {
            Validation.setValid(contactEl);
        }

        // 3. Residential Address Validation
        const addressVal = Validation.validateAddress(patientAddress);
        if (!addressVal.valid) {
            Validation.setInvalid(addressEl, addressVal.message);
            isValid = false;
        } else {
            Validation.setValid(addressEl);
        }

        // 4. Email Address Validation (Optional)
        const emailVal = Validation.validateEmail(patientEmail);
        if (!emailVal.valid) {
            Validation.setInvalid(emailEl, emailVal.message);
            isValid = false;
        } else if (patientEmail) {
            Validation.setValid(emailEl);
        } else {
            Validation.clearFieldStatus(emailEl);
        }

        // 5. Dentist Selection Validation
        const dentistVal = Validation.validateDentist(dentistId);
        if (!dentistVal.valid) {
            Validation.setInvalid(dentistEl, dentistVal.message);
            isValid = false;
        } else {
            Validation.setValid(dentistEl);
        }

        // 6. Treatment Selection Validation
        const treatmentVal = Validation.validateTreatment(treatmentId);
        if (!treatmentVal.valid) {
            Validation.setInvalid(treatmentEl, treatmentVal.message);
            isValid = false;
        } else {
            Validation.setValid(treatmentEl);
        }

        // 7. Appointment Date Validation
        const dateVal = Validation.validateDate(appointmentDate);
        if (!dateVal.valid) {
            Validation.setInvalid(dateEl, dateVal.message);
            isValid = false;
        } else {
            Validation.setValid(dateEl);
        }

        // 8. Appointment Time Validation
        const timeVal = Validation.validateTime(appointmentTime);
        if (!timeVal.valid) {
            Validation.setInvalid(timeEl, timeVal.message);
            isValid = false;
        } else {
            Validation.setValid(timeEl);
        }

        if (!isValid) {
            Validation.showAlert('alertContainer', 'Please correct the highlighted form errors before proceeding.', 'danger');
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
            patientEmail: patientEmail || null,
            dentistId,
            treatmentId,
            appointmentDate,
            appointmentTime: appointmentTime.length === 5 ? appointmentTime + ':00' : appointmentTime,
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
                Official Reference: <strong>${app.appointmentNumber}</strong><br>
                Patient: <strong>${app.patientName}</strong> (${app.patientContact})<br>
                Date & Time: ${app.appointmentDate} at ${app.appointmentTime}<br>
                Specialist: ${app.dentistName} | Treatment: ${app.treatmentName}<br>
                Estimated Total: LKR ${parseFloat(app.totalEstimatedCost).toLocaleString()}
            `, 'success');

            document.getElementById('bookingForm').reset();
            [nameEl, contactEl, addressEl, emailEl, dentistEl, treatmentEl, dateEl, timeEl].forEach(el => Validation.clearFieldStatus(el));
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
        const inputEl = document.getElementById('searchInput');
        const container = document.getElementById('searchResultCard');
        if (container) container.style.display = 'none';

        const valResult = Validation.validateAppointmentNumber(appNumber);
        if (!valResult.valid) {
            if (inputEl) Validation.setInvalid(inputEl, valResult.message);
            Validation.showAlert('searchAlert', valResult.message, 'danger');
            return;
        } else if (inputEl) {
            Validation.setValid(inputEl);
        }

        const res = await API.get(`/appointments?appointmentNumber=${encodeURIComponent(appNumber.trim().toUpperCase())}`);
        if (res.success && res.data) {
            this.renderAppointmentDetails(res.data);
        } else {
            if (inputEl) Validation.setInvalid(inputEl, 'No appointment record found matching this reference.');
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
