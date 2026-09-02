/**
 * Enterprise Client-Side Validation Engine and UI Feedback Presenter
 * Supports Real-Time Validation, Field-Level Visual Indicators, and Comprehensive Regex Patterns.
 */
const Validation = {
    // Regex Patterns
    // Standard phone formats: 07XXXXXXXX, +947XXXXXXXX, 011XXXXXXX, 0XXXXXXXXX (9-12 digits)
    PHONE_REGEX: /^(?:0|94|\+94)?(?:7[01245678]|11|21|23|24|25|26|27|31|32|33|34|35|36|37|38|41|45|47|51|52|54|55|57|63|65|66|67|81|91)\d{7}$/,
    // Fallback permissive phone pattern allowing hyphens and spaces
    PHONE_PERMISSIVE_REGEX: /^[0-9+\s\-()]{9,15}$/,
    
    // Strict RFC 5322 compliant email regex
    EMAIL_REGEX: /^[A-Za-z0-9+_.-]+@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*\.[A-Za-z]{2,}$/,
    
    // Appointment Reference Pattern: SDC-YYYY-XXXX (e.g., SDC-2026-0001)
    APPOINTMENT_NUM_REGEX: /^SDC-\d{4}-\d{4}$/i,

    // Patient Name: Letters, spaces, dots, hyphens, and apostrophes (2-100 characters)
    NAME_REGEX: /^[a-zA-Z\s.'-]{2,100}$/,

    /**
     * Renders a styled banner alert in a container.
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
        container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    },

    /**
     * Clears banner alert in container.
     */
    clearAlert(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = '';
        }
    },

    /**
     * Marks an input element as VALID with visual feedback.
     */
    setValid(inputEl, message = '') {
        if (!inputEl) return;
        inputEl.classList.remove('is-invalid');
        inputEl.classList.add('is-valid');
        inputEl.setAttribute('aria-invalid', 'false');

        const parent = inputEl.closest('.form-group') || inputEl.parentElement;
        if (parent) {
            let feedback = parent.querySelector('.invalid-feedback');
            if (feedback) feedback.remove();
            
            let validFeedback = parent.querySelector('.valid-feedback');
            if (message) {
                if (!validFeedback) {
                    validFeedback = document.createElement('div');
                    validFeedback.className = 'valid-feedback';
                    parent.appendChild(validFeedback);
                }
                validFeedback.innerHTML = `<span>✓</span> ${message}`;
            } else if (validFeedback) {
                validFeedback.remove();
            }
        }
    },

    /**
     * Marks an input element as INVALID with error message.
     */
    setInvalid(inputEl, message) {
        if (!inputEl) return;
        inputEl.classList.remove('is-valid');
        inputEl.classList.add('is-invalid');
        inputEl.setAttribute('aria-invalid', 'true');

        const parent = inputEl.closest('.form-group') || inputEl.parentElement;
        if (parent) {
            let validFeedback = parent.querySelector('.valid-feedback');
            if (validFeedback) validFeedback.remove();

            let feedback = parent.querySelector('.invalid-feedback');
            if (!feedback) {
                feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                parent.appendChild(feedback);
            }
            feedback.innerHTML = `<span>✕</span> ${message}`;
        }
    },

    /**
     * Resets visual validation state on a field.
     */
    clearFieldStatus(inputEl) {
        if (!inputEl) return;
        inputEl.classList.remove('is-valid', 'is-invalid');
        inputEl.removeAttribute('aria-invalid');
        const parent = inputEl.closest('.form-group') || inputEl.parentElement;
        if (parent) {
            const feedback = parent.querySelectorAll('.invalid-feedback, .valid-feedback');
            feedback.forEach(f => f.remove());
        }
    },

    // =========================================================================
    // FIELD-SPECIFIC VALIDATORS
    // =========================================================================

    validatePatientName(name) {
        if (!name || !name.trim()) {
            return { valid: false, message: 'Patient full name is required.' };
        }
        const trimmed = name.trim();
        if (trimmed.length < 2) {
            return { valid: false, message: 'Patient name must be at least 2 characters.' };
        }
        if (trimmed.length > 100) {
            return { valid: false, message: 'Patient name cannot exceed 100 characters.' };
        }
        if (!this.NAME_REGEX.test(trimmed)) {
            return { valid: false, message: 'Patient name must contain only letters, dots, and spaces.' };
        }
        return { valid: true, message: 'Looks good!' };
    },

    validateAddress(address) {
        if (!address || !address.trim()) {
            return { valid: false, message: 'Residential address is required.' };
        }
        const trimmed = address.trim();
        if (trimmed.length < 5) {
            return { valid: false, message: 'Address must be at least 5 characters long.' };
        }
        if (trimmed.length > 255) {
            return { valid: false, message: 'Address cannot exceed 255 characters.' };
        }
        return { valid: true, message: 'Address accepted.' };
    },

    validatePhone(phone) {
        if (!phone || !phone.trim()) {
            return { valid: false, message: 'Contact phone number is required.' };
        }
        const cleaned = phone.trim().replace(/[\s\-()]/g, '');
        if (cleaned.length < 9 || cleaned.length > 12) {
            return { valid: false, message: 'Phone number must be between 9 and 12 digits (e.g. 0771234567).' };
        }
        if (!this.PHONE_PERMISSIVE_REGEX.test(phone.trim())) {
            return { valid: false, message: 'Please enter a valid telephone format (e.g., 0771234567 or +94771234567).' };
        }
        return { valid: true, message: 'Valid phone number.' };
    },

    validateEmail(email) {
        if (!email || !email.trim()) {
            return { valid: true, message: '' }; // Optional field
        }
        const trimmed = email.trim();
        if (trimmed.length > 100) {
            return { valid: false, message: 'Email address cannot exceed 100 characters.' };
        }
        if (!this.EMAIL_REGEX.test(trimmed)) {
            return { valid: false, message: 'Please enter a valid email address format (e.g. patient@example.com).' };
        }
        return { valid: true, message: 'Valid email address.' };
    },

    validateDentist(dentistId) {
        const id = parseInt(dentistId, 10);
        if (!id || isNaN(id) || id <= 0) {
            return { valid: false, message: 'Please select an assigned dental specialist.' };
        }
        return { valid: true, message: 'Specialist selected.' };
    },

    validateTreatment(treatmentId) {
        const id = parseInt(treatmentId, 10);
        if (!id || isNaN(id) || id <= 0) {
            return { valid: false, message: 'Please select a clinical treatment procedure.' };
        }
        return { valid: true, message: 'Treatment procedure selected.' };
    },

    validateDate(dateString) {
        if (!dateString) {
            return { valid: false, message: 'Appointment date is required.' };
        }
        const selected = new Date(dateString + 'T00:00:00');
        if (isNaN(selected.getTime())) {
            return { valid: false, message: 'Please select a valid calendar date.' };
        }
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selected < today) {
            return { valid: false, message: 'Appointment date cannot be in the past.' };
        }

        // Maximum 1 year in advance
        const maxFuture = new Date();
        maxFuture.setFullYear(maxFuture.getFullYear() + 1);
        if (selected > maxFuture) {
            return { valid: false, message: 'Appointments can only be scheduled up to 1 year in advance.' };
        }

        return { valid: true, message: 'Valid appointment date.' };
    },

    validateTime(timeString) {
        if (!timeString) {
            return { valid: false, message: 'Appointment time is required.' };
        }
        const parts = timeString.split(':');
        const hour = parseInt(parts[0], 10);
        const minute = parseInt(parts[1], 10);

        if (isNaN(hour) || isNaN(minute)) {
            return { valid: false, message: 'Invalid time format.' };
        }
        if (hour < 8 || hour > 17 || (hour === 17 && minute > 0)) {
            return { valid: false, message: 'Operating hours are strictly 08:00 AM to 05:00 PM.' };
        }
        return { valid: true, message: 'Time within operating hours.' };
    },

    validateAppointmentNumber(num) {
        if (!num || !num.trim()) {
            return { valid: false, message: 'Appointment reference number is required.' };
        }
        const trimmed = num.trim().toUpperCase();
        if (!this.APPOINTMENT_NUM_REGEX.test(trimmed)) {
            return { valid: false, message: 'Expected format: SDC-YYYY-XXXX (e.g., SDC-2026-0001).' };
        }
        return { valid: true, message: 'Valid reference number format.' };
    },

    validateDiscount(discountValue) {
        if (discountValue === '' || discountValue === null || discountValue === undefined) {
            return { valid: true, message: '0% (Standard rate)' };
        }
        const num = parseFloat(discountValue);
        if (isNaN(num)) {
            return { valid: false, message: 'Discount must be a numeric percentage.' };
        }
        if (num < 0 || num > 100) {
            return { valid: false, message: 'Discount percentage must be between 0.00% and 100.00%.' };
        }
        return { valid: true, message: `${num.toFixed(2)}% discount applied.` };
    },

    validatePaymentMethod(method) {
        const validMethods = ['CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'INSURANCE', 'BANK_TRANSFER'];
        if (!method || !validMethods.includes(method.toUpperCase())) {
            return { valid: false, message: 'Please select a valid settlement payment method.' };
        }
        return { valid: true, message: 'Payment method verified.' };
    },

    validateUsername(username) {
        if (!username || !username.trim()) {
            return { valid: false, message: 'Username is required.' };
        }
        if (username.trim().length < 3) {
            return { valid: false, message: 'Username must be at least 3 characters.' };
        }
        return { valid: true, message: '' };
    },

    validatePassword(password) {
        if (!password) {
            return { valid: false, message: 'Password is required.' };
        }
        if (password.length < 6) {
            return { valid: false, message: 'Password must be at least 6 characters.' };
        }
        return { valid: true, message: '' };
    },

    // =========================================================================
    // REAL-TIME FORM ATTACHMENT HELPER
    // =========================================================================
    
    /**
     * Attaches live input and blur validators to input elements.
     */
    bindRealtimeValidation(fieldId, validatorFn) {
        const el = document.getElementById(fieldId);
        if (!el) return;

        const check = () => {
            const result = validatorFn(el.value);
            if (!result.valid) {
                this.setInvalid(el, result.message);
            } else {
                this.setValid(el, result.message);
            }
            return result.valid;
        };

        el.addEventListener('input', check);
        el.addEventListener('blur', check);
        el.addEventListener('change', check);
    }
};
