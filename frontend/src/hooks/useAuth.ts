import { useState, useEffect, useCallback } from "react"

// checks whether token is expired
const isTokenExpired = (token: string): boolean => {
    try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        const exp = payload.exp * 1000 // converting into milliseconds
        return Date.now() > exp
    } catch {
        return true // if the token cannot be parsed, it is invalid
    }
}

export const useAuth = () => {
    const [isAuthenticated, setIsAuthenticated] = useState(false)
    const [loading, setLoading] = useState(true)

    const checkAuth = useCallback(() => {
        const token = localStorage.getItem('token')

        if (!token) {
            setIsAuthenticated(false)
            setLoading(false)
            return false
        }

        // checking whether token is expired
        if (isTokenExpired(token)) {
            localStorage.removeItem('token')
            setIsAuthenticated(false)
            setLoading(false)
            return false
        }

        setIsAuthenticated(true)
        setLoading(false)
        return true
    }, [])

    useEffect(() => {
        // initial check
        checkAuth()

        // checking every minute for expiration
        const interval = setInterval(() => {
            checkAuth()
        }, 60000) // 1 minute

        // listening changes in localStorage
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key === 'token') {
                checkAuth()
            }
        }

        // listening custom event for updates from other tabs
        const handleAuthChange = () => checkAuth()

        window.addEventListener('storage', handleStorageChange)
        window.addEventListener('authChange', handleAuthChange)

        return () => {
            clearInterval(interval)
            window.removeEventListener('storage', handleStorageChange)
            window.removeEventListener('authChange', handleAuthChange)
        }
    }, [checkAuth])

    const login = useCallback((token: string) => {
        localStorage.setItem('token', token)
        setIsAuthenticated(true)
        // triggering an event for updating other components
        window.dispatchEvent(new Event('authChange'))
    }, [])

    const logout = useCallback(() => {
        localStorage.removeItem('token')
        setIsAuthenticated(false)
        window.dispatchEvent(new Event('authChange'))
    }, [])

    return {
        isAuthenticated,
        loading,
        login,
        logout,
        checkAuth,
    }
}