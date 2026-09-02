/**
 * Authentication and Session State Manager.
 */
const Auth = {
    currentUser: null,

    /**
     * Checks if current page visitor has active session. Redirects to login if not.
     */
    async requireAuth() {
        const res = await API.get('/session');
        if (res.success && res.data) {
            this.currentUser = res.data;
            this.renderUserProfile(res.data);
            return res.data;
        } else {
            window.location.href = 'login.html';
            return null;
        }
    },

    /**
     * If visitor is already logged in and visits login.html, redirect to dashboard.
     */
    async redirectIfLoggedIn() {
        const res = await API.get('/session');
        if (res.success && res.data) {
            window.location.href = 'dashboard.html';
        }
    },

    /**
     * Handles login form submit.
     */
    async login(username, password) {
        return await API.post('/login', { username, password });
    },

    /**
     * Handles logout request and invalidates session.
     */
    async logout() {
        if (confirm('Are you sure you want to log out?')) {
            await API.post('/logout', {});
            window.location.href = 'login.html';
        }
    },

    /**
     * Injects active user details into sidebar/header.
     */
    renderUserProfile(user) {
        const nameElements = document.querySelectorAll('.user-name');
        const roleElements = document.querySelectorAll('.user-role');
        const avatarElements = document.querySelectorAll('.user-avatar');

        nameElements.forEach(el => el.textContent = user.fullName || user.username);
        roleElements.forEach(el => el.textContent = user.role || 'STAFF');
        avatarElements.forEach(el => {
            const initials = (user.fullName || user.username).substring(0, 2).toUpperCase();
            el.textContent = initials;
        });

        // Hide reports tab for non-admin users
        if (user.role !== 'ADMIN') {
            const reportsNav = document.getElementById('nav-reports');
            if (reportsNav) {
                reportsNav.style.display = 'none';
            }
        }
    }
};

// Global Logout event listener
document.addEventListener('DOMContentLoaded', () => {
    const logoutBtn = document.getElementById('btnLogout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }
});
