import { useState, useEffect } from 'react';

export const useAuth = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const checkAuth = () => {
            const token = localStorage.getItem('token');
            setIsAuthenticated(!!token);
            setLoading(false);
        };

        // initial check
        checkAuth();

        // listening changes in localStorage
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key === 'token') {
                checkAuth();
            }
        };

        // listening custom event for updates from other tabs
        const handleAuthChange = () => checkAuth();

        window.addEventListener('storage', handleStorageChange);
        window.addEventListener('authChange', handleAuthChange);

        return () => {
            window.removeEventListener('storage', handleStorageChange);
            window.removeEventListener('authChange', handleAuthChange);
        };
    }, []);

    const login = (token: string) => {
        localStorage.setItem('token', token);
        setIsAuthenticated(true);
        // triggering an event for updating other components
        window.dispatchEvent(new Event('authChange'));
    };

    const logout = () => {
        localStorage.removeItem('token');
        setIsAuthenticated(false);
        window.dispatchEvent(new Event('authChange'));
    };

    return {
        isAuthenticated,
        loading,
        login,
        logout,
    };
};