/**
 * Decision-Support Reporting Client Script.
 */
const ReportsManager = {

    async loadReports() {
        try {
            const [dailyRes, monthlyRes, dentistRes] = await Promise.all([
                API.get('/reports?type=daily'),
                API.get('/reports?type=monthly'),
                API.get('/reports?type=dentist')
            ]);

            if (dailyRes.success) {
                this.renderDailyTable(dailyRes.data);
            }
            if (monthlyRes.success) {
                this.renderMonthlyTable(monthlyRes.data);
            }
            if (dentistRes.success) {
                this.renderDentistTable(dentistRes.data);
            }
        } catch (error) {
            console.error('Error loading reports:', error);
        }
    },

    renderDailyTable(data) {
        const tbody = document.getElementById('dailyReportTbody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#64748b;">No daily data recorded yet.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        data.forEach(row => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${row.appointmentDate}</strong></td>
                <td>${row.dentistName}</td>
                <td>${row.specialization}</td>
                <td><span class="badge badge-scheduled">${row.totalScheduledAppointments}</span></td>
                <td><span class="badge badge-completed">${row.completedAppointments}</span></td>
                <td><span class="badge badge-cancelled">${row.cancelledAppointments}</span></td>
                <td><strong>LKR ${parseFloat(row.dailyRevenueGenerated).toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong></td>
            `;
            tbody.appendChild(tr);
        });
    },

    renderMonthlyTable(data) {
        const tbody = document.getElementById('monthlyReportTbody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">No monthly financial records.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        data.forEach(row => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${row.revenueMonth}</strong></td>
                <td>${row.totalInvoicesIssued}</td>
                <td>LKR ${parseFloat(row.totalConsultationFees).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td>LKR ${parseFloat(row.totalTreatmentCosts).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td>LKR ${parseFloat(row.totalDiscountsGranted).toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                <td><strong style="color:#059669;">LKR ${parseFloat(row.netRevenue).toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong></td>
            `;
            tbody.appendChild(tr);
        });
    },

    renderDentistTable(data) {
        const tbody = document.getElementById('dentistReportTbody');
        if (!tbody) return;

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;color:#64748b;">No dentist performance records.</td></tr>';
            return;
        }

        tbody.innerHTML = '';
        data.forEach(row => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>${row.dentistName}</strong></td>
                <td>${row.specialization}</td>
                <td>${row.uniquePatientsServed}</td>
                <td>${row.totalAssignedAppointments}</td>
                <td><span class="badge badge-completed">${row.successfulTreatments}</span></td>
                <td><strong style="color:#0284c7;">LKR ${parseFloat(row.totalRevenueGenerated).toLocaleString('en-US', { minimumFractionDigits: 2 })}</strong></td>
            `;
            tbody.appendChild(tr);
        });
    }
};
