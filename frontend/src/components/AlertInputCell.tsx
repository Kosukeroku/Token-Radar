import { useState, useEffect, useRef } from "react"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { X } from "lucide-react"
import { api } from "@/services/api"
import { cn } from "@/lib/utils"

interface AlertInputCellProps {
    coinId: string
    coinName: string
    currentPrice: number
    type: "downPercentage" | "upPercentage" | "downPrice" | "upPrice"
}

export function AlertInputCell({
                                   coinId,
                                   currentPrice,
                                   type,
                               }: AlertInputCellProps) {
    const [value, setValue] = useState("")
    const [originalValue, setOriginalValue] = useState("")
    const [alertId, setAlertId] = useState<number | null>(null)
    const [isEditing, setIsEditing] = useState(false)
    const [error, setError] = useState("")
    const [showError, setShowError] = useState(false)
    const [errorTimer, setErrorTimer] = useState<NodeJS.Timeout | null>(null)
    const inputRef = useRef<HTMLInputElement>(null)
    const containerRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        loadValue()
        return () => {
            if (errorTimer) clearTimeout(errorTimer)
        }
    }, [coinId])

    const loadValue = async () => {
        try {
            const response = await api.get('/alerts')
            const alerts = response.data || []

            const typeMap = {
                upPercentage: "percentage_up",
                downPercentage: "percentage_down",
                upPrice: "price_above",
                downPrice: "price_below"
            } as const

            const alertType = typeMap[type]
            const alert = alerts.find((a: any) =>
                a.coinId === coinId && a.type === alertType
            )

            if (alert) {
                setAlertId(alert.id)
                setValue(String(alert.thresholdValue))
            } else {
                setAlertId(null)
                setValue(type === 'downPercentage' ? "-0" : "0")
            }
            setError("")
            setShowError(false)
        } catch (error) {
            console.error('Load error:', error)
            setAlertId(null)
            setValue(type === 'downPercentage' ? "-0" : "0")
        }
    }

    // basic validation
    const validateValue = (val: string): boolean => {
        const num = parseFloat(val)
        if (isNaN(num)) return false

        switch (type) {
            case "upPercentage":
                return num > 0 && num <= 1000
            case "downPercentage":
                return num < 0 && num >= -100
            case "upPrice":
                return num > currentPrice && num <= 1000000000
            case "downPrice":
                return num > 0 && num < currentPrice
            default:
                return true
        }
    }

    const startErrorTimer = (val: string) => {
        // resetting previous timer
        if (errorTimer) {
            clearTimeout(errorTimer)
        }

        // starting a new timer
        const timer = setTimeout(() => {
            const isValid = validateValue(val)
            if (!isValid) {
                setError(getErrorMessage(val))
                setShowError(true)
            }
        }, 500) // 0,5 sec

        setErrorTimer(timer)
    }

    const getErrorMessage = (val: string): string => {
        const num = parseFloat(val)
        if (isNaN(num)) return "Enter a valid number"

        switch (type) {
            case "upPercentage":
                if (num <= 0) return "Must be positive!"
                if (num > 1000) return "Cannot exceed 1000%!"
                break
            case "downPercentage":
                if (num >= 0) return "Must be negative!"
                if (num < -100) return "Cannot exceed -100%!"
                break
            case "upPrice":
                if (num <= currentPrice) return `Must be above $${currentPrice.toFixed(2)}!`
                if (num > 1000000000) return "Cannot exceed $1,000,000,000!"
                break
            case "downPrice":
                if (num <= 0) return "Must be > 0!"
                if (num >= currentPrice) return `Must be below $${currentPrice.toFixed(2)}!`
                break
        }

        return "Invalid value"
    }

    const saveValue = async (val: string) => {
        const num = parseFloat(val)
        if (isNaN(num)) return

        try {
            const typeMapSend = {
                upPercentage: "PERCENTAGE_UP",
                downPercentage: "PERCENTAGE_DOWN",
                upPrice: "PRICE_ABOVE",
                downPrice: "PRICE_BELOW"
            } as const

            const response = await api.post('/alerts', {
                coinId,
                type: typeMapSend[type],
                thresholdValue: num
            })

            if (response.data?.id) {
                setAlertId(response.data.id)
                setValue(String(response.data.thresholdValue))
                setError("")
                setShowError(false)
            } else {
                setTimeout(loadValue, 300)
            }

        } catch (error: any) {
            console.error('Save error:', error)

            const errorMessage = error.response?.data?.message ||
                error.response?.data ||
                "Failed to save"

            setError(errorMessage)
            setShowError(true)

            // resetting back to the original value
            setValue(originalValue)
        }
    }

    const deleteValue = async () => {
        if (!alertId) return

        try {
            await api.delete(`/alerts/${alertId}`)
            setAlertId(null)
            setValue(type === 'downPercentage' ? "-0" : "0")
            setError("")
            setShowError(false)
        } catch (error: any) {
            console.error('Delete error:', error)
        }
    }

    const startEditing = () => {
        setOriginalValue(value) // saving the original value
        setError("")
        setShowError(false)
        setIsEditing(true)
        setTimeout(() => {
            inputRef.current?.focus()
            inputRef.current?.select()
        }, 10)
    }

    const cancelEditing = () => {
        // back to the original value
        setValue(originalValue)
        setError("")
        setShowError(false)
        setIsEditing(false)
        if (errorTimer) {
            clearTimeout(errorTimer)
            setErrorTimer(null)
        }
    }

    const confirmEditing = () => {
        const trimmed = value.trim()

        // if the value has not changed
        if (trimmed === originalValue) {
            setIsEditing(false)
            if (errorTimer) {
                clearTimeout(errorTimer)
                setErrorTimer(null)
            }
            return
        }

        // rollback in case of an error
        if (showError && error) {
            cancelEditing()
            return
        }

        // if empty or 0
        if (!trimmed || trimmed === "0" || trimmed === "-0") {
            if (alertId) {
                deleteValue()
            } else {
                setValue(type === 'downPercentage' ? "-0" : "0")
            }
            setIsEditing(false)
            if (errorTimer) {
                clearTimeout(errorTimer)
                setErrorTimer(null)
            }
            return
        }

        // if not a number
        if (isNaN(parseFloat(trimmed))) {
            cancelEditing()
            return
        }

        // saving
        saveValue(trimmed)
        setIsEditing(false)
        if (errorTimer) {
            clearTimeout(errorTimer)
            setErrorTimer(null)
        }
    }

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            confirmEditing()
        } else if (e.key === 'Escape') {
            cancelEditing()
        }
    }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const newValue = e.target.value
        setValue(newValue)

        // clearing the error on edit
        if (error) {
            setError("")
            setShowError(false)
        }

        // launching a check timer
        if (errorTimer) {
            clearTimeout(errorTimer)
        }
        startErrorTimer(newValue)
    }

    const formatDisplay = (val: string) => {
        const num = parseFloat(val)
        if (isNaN(num) || num === 0) {
            return type === 'downPercentage' ? "-0%" : type.includes('Percentage') ? "0%" : "$0"
        }

        if (type.includes('Percentage')) {
            if (type === 'downPercentage') {
                return `-${Math.abs(num)}%`
            }
            return `${num > 0 ? '+' : ''}${num}%`
        } else {
            return `$${num.toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 8
            })}`
        }
    }

    const hasAlert = alertId !== null
    const display = formatDisplay(value)

    return (
        <div ref={containerRef} className="relative">
            {isEditing ? (
                <div className="relative">
                    <Input
                        ref={inputRef}
                        value={value}
                        onChange={handleChange}
                        onBlur={confirmEditing}
                        onKeyDown={handleKeyDown}
                        className={cn(
                            "h-7 w-[90px] text-xs text-center px-6",
                            showError && "border-destructive"
                        )}
                        autoFocus
                    />

                    {showError && error && (
                        <div className="absolute left-0 right-0 mt-0.5 z-50">
                            <div className="text-xs text-destructive whitespace-nowrap">
                                {error}
                            </div>
                        </div>
                    )}
                </div>
            ) : (
                <div className="relative inline-flex">
                    <div
                        className={cn(
                            "min-w-[90px] h-7 px-6 flex items-center justify-center text-xs rounded border cursor-pointer",
                            hasAlert
                                ? "border-primary/50 bg-primary/5"
                                : "border-border text-muted-foreground"
                        )}
                        onClick={startEditing}
                    >
                        {display}
                    </div>

                    {hasAlert && (
                        <Button
                            type="button"
                            size="icon"
                            variant="ghost"
                            onClick={(e) => {
                                e.stopPropagation()
                                deleteValue()
                            }}
                            className="absolute right-0 top-1/2 -translate-y-1/2 h-5 w-5 hover:bg-destructive/10 hover:text-destructive"
                        >
                            <X className="h-3 w-3" />
                        </Button>
                    )}
                </div>
            )}
        </div>
    )
}