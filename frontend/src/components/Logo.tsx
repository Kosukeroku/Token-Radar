import { Link } from "react-router-dom"

interface LogoProps {
    size?: "sm" | "md" | "lg"
    showText?: boolean
    className?: string
}

export function Logo({ size = "md", showText = true, className = "" }: LogoProps) {
    const sizeClasses = {
        sm: "h-8 w-8",
        md: "h-10 w-10",
        lg: "h-12 w-12",
    }

    const textClasses = {
        sm: "text-lg",
        md: "text-xl",
        lg: "text-2xl",
    }

    return (
        <Link
            to="/"
            className={`flex items-center gap-3 hover:opacity-90 transition-opacity ${className}`}
            onClick={(e) => {
                if (window.location.pathname === '/') {
                    e.preventDefault()
                    window.location.href = '/' // full reload to clear search
                }
            }}
        >
            <img
                src="/logo.png"
                alt="Token Radar Logo"
                className={`${sizeClasses[size]} rounded-lg logo-hover`}
                onError={(e) => {
                    const target = e.target as HTMLImageElement
                    target.style.display = 'none'
                    const parent = target.parentElement
                    if (parent) {
                        parent.innerHTML = `
              <div class="${sizeClasses[size]} rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center logo-hover">
                <span class="text-white font-bold ${textClasses[size]}">TR</span>
              </div>
              ${showText ? '<h1 class="font-bold tracking-tight font-serif italic ' + textClasses[size] + '">Token Radar</h1>' : ''}
            `
                    }
                }}
            />
            {showText && (
                <h1 className={`font-bold tracking-tight font-serif italic hidden sm:block ${textClasses[size]}`}>
                    Token Radar
                </h1>
            )}
        </Link>
    )
}