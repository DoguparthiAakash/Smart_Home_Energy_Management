/**
 * Auth Guard
 * Checks if the user is logged in (token exists).
 * Redirects to /login if not authenticated.
 * Redirects to /dashboard if already authenticated (for login/register pages).
 */
(function () {
    const token = localStorage.getItem('token');
    const path = window.location.pathname;

    const publicPages = ['/login', '/register', '/verify-2fa', '/'];
    const isPublicPage = publicPages.includes(path);

    if (!token && !isPublicPage) {
        // Not logged in, trying to access protected page
        console.log("AuthGuard: No token found. Redirecting to login.");
        window.location.href = '/login';
    } else if (token) {
        // Decode token to get Role
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            const payload = JSON.parse(jsonPayload);

            // Store role globally/locally for UI to use
            if (payload.role) {
                localStorage.setItem('userRole', payload.role); // e.g. ROLE_ADMIN
            }
        } catch (e) { console.error("Error parsing token", e); }

        if (path === '/login' || path === '/register' || path === '/') {
            // Logged in, trying to access login/register
            console.log("AuthGuard: Token found. Redirecting to dashboard.");
            window.location.href = '/dashboard';
        }
    }
})();
