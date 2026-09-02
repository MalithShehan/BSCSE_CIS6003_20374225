/**
 * Unified Fetch API Client for Sunrise Dental Clinic REST Endpoints.
 */
const API = {
    BASE_URL: (window.location.pathname.includes('/sunrise-dental-clinic') ? '/sunrise-dental-clinic/api' : '/api'),

    /**
     * Executes HTTP Request with JSON parsing and session credential propagation.
     */
    async request(endpoint, options = {}) {
        const url = `${this.BASE_URL}${endpoint}`;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            },
            credentials: 'include' // Propagates JSESSIONID cookie
        };

        try {
            const response = await fetch(url, config);
            let result = null;

            try {
                result = await response.json();
            } catch (jsonError) {
                result = { success: false, message: `Server error (${response.status})`, data: null };
            }

            // If session expired and trying to access protected endpoint from a protected page
            const isLoginPage = window.location.pathname.endsWith('login.html') || window.location.pathname === '' || window.location.pathname.endsWith('/sunrise-dental-clinic/');
            if (response.status === 401 && !endpoint.includes('/login') && !endpoint.includes('/session') && !isLoginPage) {
                window.location.href = 'login.html?expired=true';
                return { success: false, message: 'Session expired. Please log in again.' };
            }

            return {
                status: response.status,
                ok: response.ok,
                ...result
            };
        } catch (networkError) {
            console.error('API Network Failure:', networkError);
            return {
                status: 0,
                ok: false,
                success: false,
                message: 'Unable to connect to clinic server. Please ensure backend is running.'
            };
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};
