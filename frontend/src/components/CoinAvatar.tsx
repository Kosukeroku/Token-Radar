import React, { useState } from 'react';

interface CoinAvatarProps {
    imageUrl?: string;
    symbol: string;
    size?: number;
    className?: string;
}

const CoinAvatar: React.FC<CoinAvatarProps> = ({
                                                   imageUrl,
                                                   symbol,
                                                   size = 32,
                                                   className = ''
                                               }) => {
    const [imgError, setImgError] = useState(false);

    // first 2-3 symbols for initials
    const initials = symbol.length >= 3
        ? symbol.substring(0, 3).toUpperCase()
        : symbol.toUpperCase();

    // generating color
    const bgColor = generateColorFromString(symbol);

    // showing svg in case of an error or if no image present
    if (!imageUrl || imgError) {
        return (
            <div
                className={`rounded-full flex items-center justify-center text-white font-semibold ${className}`}
                style={{
                    width: size,
                    height: size,
                    backgroundColor: bgColor,
                    fontSize: Math.max(10, size * 0.35)
                }}
                title={symbol.toUpperCase()}
            >
                {initials}
            </div>
        );
    }

    // showing an image if there is one
    return (
        <img
            src={imageUrl}
            alt={symbol}
            className={`rounded-full ${className}`}
            style={{ width: size, height: size }}
            onError={() => setImgError(true)}
            loading="lazy"
        />
    );
};

// generates color based on the string
const generateColorFromString = (str: string): string => {
    // hash function for getting a stable number from string
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
        hash = str.charCodeAt(i) + ((hash << 5) - hash);
    }

    // transforming hash into color index
    const colorIndex = Math.abs(hash) % COLOR_PALETTE.length;
    return COLOR_PALETTE[colorIndex];
};

const COLOR_PALETTE = [
    '#3B82F6', // blue-500
    '#10B981', // emerald-500
    '#F59E0B', // amber-500
    '#EF4444', // red-500
    '#8B5CF6', // violet-500
    '#EC4899', // pink-500
    '#14B8A6', // teal-500
    '#F97316', // orange-500
    '#6366F1', // indigo-500
    '#8B5CF6', // purple-500
    '#06B6D4', // cyan-500
    '#84CC16', // lime-500
];

export default CoinAvatar;