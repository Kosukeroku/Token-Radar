import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const Header: React.FC = () => {
    const navigate = useNavigate();
    const { isAuthenticated, loading, logout } = useAuth();

    const handleLogin = () => {
        navigate('/login');
    };

    const handleSignUp = () => {
        navigate('/register');
    };

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    // showing spinner while loading auth status
    if (loading) {
        return (
            <header className="bg-gray-800 border-b border-gray-700 py-3 px-4 md:px-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                        <Link to="/" className="flex items-center space-x-2 hover:opacity-90 transition-opacity">
                            <img
                                src="/logo.png"
                                alt="Token Radar Logo"
                                className="w-10 h-10"
                            />
                            <h1 className="text-2xl font-bold text-white font-serif italic hidden md:block">
                                Token Radar
                            </h1>
                        </Link>
                    </div>
                    <div className="w-24 h-8 bg-gray-700 rounded animate-pulse"></div>
                </div>
            </header>
        );
    }

    return (
        <header className="bg-gray-800 border-b border-gray-700 py-3 px-4 md:px-6">
            <div className="flex items-center justify-between">
                {/* logo and name */}
                <div className="flex items-center space-x-3">
                    <Link to="/" className="flex items-center space-x-2 hover:opacity-90 transition-opacity">
                        <img
                            src="/logo.png"
                            alt="Token Radar Logo"
                            className="w-10 h-10"
                        />
                        <h1 className="text-2xl font-bold text-white font-serif italic hidden md:block">
                            Token Radar
                        </h1>
                        <h1 className="text-2xl font-bold text-white font-serif italic md:hidden">
                            TR
                        </h1>
                    </Link>
                </div>

                {/* authorization buttons */}
                <div className="flex items-center space-x-3">
                    {isAuthenticated ? (
                        <>
                            <Link
                                to="/profile"
                                className="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg text-sm font-medium transition-colors"
                            >
                                My Profile
                            </Link>
                            <button
                                onClick={handleLogout}
                                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-sm font-medium transition-colors"
                            >
                                Log Out
                            </button>
                        </>
                    ) : (
                        <>
                            <button
                                onClick={handleLogin}
                                className="px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg text-sm font-medium transition-colors"
                                aria-label="Log in to your account"
                            >
                                Log In
                            </button>
                            <button
                                onClick={handleSignUp}
                                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium transition-colors"
                                aria-label="Sign up for Token Radar"
                            >
                                Sign Up
                            </button>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
};

export default Header;