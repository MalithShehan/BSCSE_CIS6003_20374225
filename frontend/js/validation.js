/**
 * Client-Side Validation Utilities and UI Alert Presenter.
 */
const Validation = {
    // Regular Expressions
    PHONE_REGEX: /^[0-9+\s\-()]{9,20}$/,
    EMAIL_REGEX: /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/,
    APPOINTMENT_NUM_REGEX: /^SDC-\d{4}-\d{4}$/,

    /**
     * Renders banner alert in container.
     */
    showAlert(containerId, message, type = 'danger') {
        const container = document.getElementById(containerId);
        if (!container) return;

        const iconMap = {
            success: '✅',
            danger: '⚠️',
            warning: '⚡',
            info: 'ℹ️'
        };

        container.innerHTML = `
            <div class="alert alert-${type}">
                <span>${iconMap[type] || 'ℹ️'}</span>
                <div>${message}</div>
            </div>
        `;
        container.scrollIntoView({ behavior: 'smooth', block: 'center' });
    },

    /**
     * Clears banner alerts.
     */
    clearAlert(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = '';
        }
    },

    /**
     * Validates that date is today or in future.
     */
    isFutureOrToday(dateString) {
        if (!dateString) return false;
        const selected = new Date(dateString + 'T00:00:00');
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return selected >= today;
    },

    /**
     * Validates that time is within clinic hours (08:00 to 17:00).
     */
    isWithinClinicHours(timeString) {
        if (!timeString) return false;
        const parts = timeString.split(':');
        const hour = parseInt(parts[0], 10);
        const minute = parseInt(parts[1], 10);

        if (isNaN(hour) || isNaN(minute)) return false;
        if (hour < 8 || hour > 17) return false;
        if (hour === 17 && minute > 0) return false;
        return true;
    },

    /**
     * Validates contact number format.
     */
    isValidPhone(phone) {
        return phone && this.PHONE_REGEX.test(phone.trim());
    },

    /**
     * Validates appointment number format.
     */
    isValidAppointmentNumber(num) {
        return num && this.APPOINTMENT_NUM_REGEX.test(num.trim().toUpperCase());
    }
};
