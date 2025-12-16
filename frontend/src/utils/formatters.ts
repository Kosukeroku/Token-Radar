export const formatPercentage = (value: number | undefined): string => {
    if (value === undefined || value === null) return '0.00%';
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
};

export const formatNumber = (num: number | undefined): string => {
    if (num === undefined || num === null) return '0';

    if (num >= 1e9) {
        return (num / 1e9).toFixed(2) + 'B';
    }
    if (num >= 1e6) {
        return (num / 1e6).toFixed(2) + 'M';
    }
    if (num >= 1e3) {
        return (num / 1e3).toFixed(2) + 'K';
    }
    return num.toFixed(2);
};

export const formatDate = (dateString: string): string => {
    try {
        if (!dateString) return 'Unknown';

        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return dateString;
        }
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    } catch {
        return dateString || 'Unknown';
    }
};

export const formatDateTime = (dateString: string): string => {
    try {
        if (!dateString) return 'Recently';

        // checking different formats
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            // if not parseable
            return 'Recently';
        }

        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMinutes = Math.floor(diffMs / (1000 * 60));

        // "Just now" for anything less than 2 mins
        if (diffMinutes < 2) {
            return 'Just now';
        }
        // "X minutes ago" for 2-60 mins
        if (diffMinutes < 60) {
            return `${diffMinutes} minutes ago`;
        }

        // otherwise, full date
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    } catch {
        return 'Recently';
    }
};

export const formatPrice = (price: number | undefined): string => {
    if (price === undefined || price === null) return '$0.00';

    // different amount of digits for different prices
    if (price < 0.000001) {
        // below 0.000001$: scientific notation
        return `$${price.toExponential(4)}`;
    }
    if (price < 0.001) {
        // 0.000001$ - 0.001$: 8 digits
        return `$${price.toFixed(8).replace(/\.?0+$/, '')}`;
    }
    if (price < 0.01) {
        // 0.001$ - 0.01$: 6 digits
        return `$${price.toFixed(6).replace(/\.?0+$/, '')}`;
    }
    if (price < 1) {
        // 0.01$ - 1$: 4 digits
        return `$${price.toFixed(4).replace(/\.?0+$/, '')}`;
    }
    if (price < 10) {
        // 1$ - 10$: 4 digits
        return `$${price.toFixed(4).replace(/\.?0+$/, '')}`;
    }
    if (price < 1000) {
        // 10$ - 1000$: 2 digits
        return `$${price.toFixed(2)}`;
    }
    // > 1000$: 2 digits with thousands separator
    return `$${price.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })}`;
};

// compact version for the table
export const formatPriceCompact = (price: number | undefined): string => {
    if (price === undefined || price === null) return '$0';

    if (price < 0.0001) {
        return `$${price.toExponential(2)}`;
    }
    if (price < 10) {
        // below 10$ - 4 digits
        return `$${price.toFixed(4).replace(/\.?0+$/, '')}`;
    }
    if (price < 1000) {
        return `$${price.toFixed(2)}`;
    }
    return `$${price.toLocaleString('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2,
    })}`;
};

// for the details page
export const formatPriceDetailed = (price: number | undefined): string => {
    if (price === undefined || price === null) return '$0.00';

    if (price < 0.000001) {
        return `$${price.toExponential(6)}`;
    }
    if (price < 0.001) {
        return `$${price.toFixed(10).replace(/\.?0+$/, '')}`;
    }
    if (price < 0.01) {
        return `$${price.toFixed(8).replace(/\.?0+$/, '')}`;
    }
    if (price < 10) {
        return `$${price.toFixed(6).replace(/\.?0+$/, '')}`;
    }
    if (price < 1000) {
        return `$${price.toFixed(4).replace(/\.?0+$/, '')}`;
    }
    return `$${price.toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    })}`;
};
export const formatMarketCap = (marketCap: number | undefined): string => {
    if (marketCap === undefined || marketCap === null) return '$0';
    if (marketCap >= 1e12) return `$${(marketCap / 1e12).toFixed(2)}T`;
    if (marketCap >= 1e9) return `$${(marketCap / 1e9).toFixed(2)}B`;
    if (marketCap >= 1e6) return `$${(marketCap / 1e6).toFixed(2)}M`;
    return `$${marketCap.toLocaleString('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    })}`;

};

export const formatVolume = (volume: number | undefined): string => {
    if (volume === undefined || volume === null) return '$0';
    return `$${formatNumber(volume)}`;
};

export const timeAgo = (dateString: string): string => {
    try {
        if (!dateString) return 'unknown';

        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return 'unknown';
        }

        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
        const diffMonths = Math.floor(diffDays / 30);
        const diffYears = Math.floor(diffDays / 365);

        if (diffYears > 0) {
            return `${diffYears} year${diffYears > 1 ? 's' : ''}`;
        } else if (diffMonths > 0) {
            return `${diffMonths} month${diffMonths > 1 ? 's' : ''}`;
        } else if (diffDays > 0) {
            return `${diffDays} day${diffDays > 1 ? 's' : ''}`;
        } else {
            const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
            if (diffHours > 0) {
                return `${diffHours} hour${diffHours > 1 ? 's' : ''}`;
            } else {
                return 'today';
            }
        }
    } catch {
        return 'unknown';
    }
};
