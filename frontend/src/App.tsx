import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from "@/components/ui/toaster"
import { Header } from './components/Header'
import { Dashboard } from './components/Dashboard'
import { CoinDetail } from './components/coin-detail/CoinDetail'
import { Login } from './components/Login'
import { Register } from './components/Register'
import { Profile } from './components/Profile'
import { VersionFooter } from './components/VersionFooter'
import ErrorBoundary from './components/ErrorBoundary'
import './index.css'

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: 1,
            refetchOnWindowFocus: false,
        },
    },
})

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <ErrorBoundary>
                <Router>
                    <div className="min-h-screen bg-background text-foreground dark">
                        <Header />
                        <Routes>
                            <Route path="/" element={<Dashboard />} />
                            <Route path="/coin/:coinId" element={<CoinDetail />} />
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />
                            <Route path="/profile" element={<Profile />} />
                        </Routes>
                        <VersionFooter />
                        <Toaster />
                    </div>
                </Router>
            </ErrorBoundary>
        </QueryClientProvider>
    )
}

export default App