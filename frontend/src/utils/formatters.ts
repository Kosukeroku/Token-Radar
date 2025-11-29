export const formatPrice = (price: number): string => {
    if (price >= 1) {
        return `$${price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
    } else {
        return `$${price.toFixed(6)}`
    }
}

export const formatMarketCap = (marketCap: number): string => {
    if (marketCap >= 1e12) {
        return `$${(marketCap / 1e12).toFixed(2)}T`
    } else if (marketCap >= 1e9) {
        return `$${(marketCap / 1e9).toFixed(2)}B`
    } else {
        return `$${(marketCap / 1e6).toFixed(2)}M`
    }
}

export const formatPercentage = (value: number): string => {
    return `${value?.toFixed(2)}%`
}