import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/hooks/useAuth"
import { Skeleton } from "@/components/ui/skeleton"
import { LogOut } from "lucide-react"
import { Logo } from "./Logo"

export function Header() {
    const navigate = useNavigate()
    const { isAuthenticated, loading, logout } = useAuth()

    const handleLogin = () => navigate('/login')
    const handleSignUp = () => navigate('/register')

    if (loading) {
        return (
            <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
                <div className="container mx-auto flex h-16 items-center justify-between px-4 sm:px-6">
                    <Skeleton className="h-8 w-32" />
                    <Skeleton className="h-9 w-24" />
                </div>
            </header>
        )
    }

    return (
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
            <div className="container mx-auto flex h-16 items-center justify-between px-4 sm:px-6">
                {/* logo */}
                <Logo size="md" showText={true} />

                {/* navigation */}
                <div className="flex items-center gap-3">
                    {isAuthenticated ? (
                        <>
                            <Button
                                variant="ghost"
                                onClick={() => navigate('/profile')}
                            >
                                My Profile
                            </Button>
                            <Button
                                variant="ghost"
                                onClick={logout}
                                className="text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/20"
                            >
                                <LogOut className="h-4 w-4 mr-2" />
                                Logout
                            </Button>
                        </>
                    ) : (
                        <>
                            <Button variant="ghost" onClick={handleLogin}>
                                Log In
                            </Button>
                            <Button onClick={handleSignUp}>
                                Sign Up
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </header>
    )
}