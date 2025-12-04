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
    if (price < 0.01) return `$${price.toFixed(6)}`;
    if (price < 1) return `$${price.toFixed(4)}`;
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
