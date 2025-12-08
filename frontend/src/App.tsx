import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Dashboard from './components/Dashboard';
import CoinDetail from './components/CoinDetail';
import Header from './components/Header';
import ErrorBoundary from './components/ErrorBoundary';
import Login from "./components/Login";
import Register from "./components/Register";
import Profile from "./components/Profile";
import './index.css';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
});

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <ErrorBoundary>
                <Router>
                    <div className="min-h-screen bg-gray-900">
                        <Header />
                        <Routes>
                            <Route path="/" element={<Dashboard />} />
                            <Route path="/coin/:coinId" element={<CoinDetail />} />
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />
                            <Route path="/profile" element={<Profile />} />
                        </Routes>
                    </div>
                </Router>
            </ErrorBoundary>
        </QueryClientProvider>
    );
}

export default App;