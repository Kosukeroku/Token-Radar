import React, { useRef, useEffect, useState } from 'react';

interface SparklineChartProps {
    data: number[];
    isPositive: boolean;
    showAxes?: boolean;
}

export const SparklineChart: React.FC<SparklineChartProps> = ({
                                                                  data,
                                                                  isPositive,
                                                                  showAxes = true
                                                              }) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [dimensions, setDimensions] = useState({ width: 600, height: 250 });

    useEffect(() => {
        const updateDimensions = () => {
            if (containerRef.current) {
                const { width } = containerRef.current.getBoundingClientRect();
                setDimensions({
                    width: Math.max(width, 400), // Минимальная ширина 400px
                    height: 250
                });
            }
        };

        updateDimensions();
        window.addEventListener('resize', updateDimensions);

        return () => window.removeEventListener('resize', updateDimensions);
    }, []);

    if (data.length === 0) return null;

    const max = Math.max(...data);
    const min = Math.min(...data);
    const range = max - min;
    const mid = min + range / 2;
    const quarter1 = min + range * 0.25;
    const quarter3 = min + range * 0.75;

    const { width, height } = dimensions;
    const padding = 50;

    const points = data.map((value, index) => {
        const x = (index / (data.length - 1)) * (width - 2 * padding) + padding;
        const y = height - padding - ((value - min) / range) * (height - 2 * padding);
        return `${x},${y}`;
    }).join(' ');

    // formatting numbers for axes
    const formatAxisValue = (value: number) => {
        if (value >= 1000000) return `$${(value / 1000000).toFixed(1)}M`;
        if (value >= 1000) return `$${(value / 1000).toFixed(1)}K`;
        return `$${value.toFixed(2)}`;
    };

    // time labels
    const getTimeLabels = () => {
        const baseLabels = ['7d ago', '6d ago', '5d ago', '4d ago', '3d ago', '2d ago', '1d ago', 'Now'];

        if (width < 500) {
            return ['7d', '5d', '3d', '1d', 'Now']; // fewer labels for smaller screens
        }
        return baseLabels;
    };

    const timeLabels = getTimeLabels();

    return (
        <div ref={containerRef} className="w-full">
            <svg
                width="100%"
                height={height}
                viewBox={`0 0 ${width} ${height}`}
                className="overflow-visible"
                role="img"
                aria-label="7-day price chart"
            >
                {/* line gradient */}
                <defs>
                    <linearGradient id="lineGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor={isPositive ? '#10B981' : '#EF4444'} stopOpacity="0.3" />
                        <stop offset="100%" stopColor={isPositive ? '#10B981' : '#EF4444'} stopOpacity="0" />
                    </linearGradient>
                </defs>

                {/* axes */}
                {showAxes && (
                    <>
                        {/* y-axis */}
                        <line
                            x1={width - padding}
                            y1={height - padding}
                            x2={width - padding}
                            y2={padding}
                            stroke="#4B5563"
                            strokeWidth="1"
                        />

                        {/* x-axis */}
                        <line
                            x1={padding}
                            y1={height - padding}
                            x2={width - padding}
                            y2={height - padding}
                            stroke="#4B5563"
                            strokeWidth="1"
                        />

                        {/* Y-axis labels */}
                        <text
                            x={width - padding + 5}
                            y={padding}
                            textAnchor="start"
                            fill="#9CA3AF"
                            fontSize="10"
                            aria-hidden="true"
                        >
                            {formatAxisValue(max)}
                        </text>
                        <text
                            x={width - padding + 5}
                            y={padding + (height - 2 * padding) * 0.25}
                            textAnchor="start"
                            fill="#9CA3AF"
                            fontSize="10"
                            aria-hidden="true"
                        >
                            {formatAxisValue(quarter1)}
                        </text>
                        <text
                            x={width - padding + 5}
                            y={height / 2}
                            textAnchor="start"
                            fill="#9CA3AF"
                            fontSize="10"
                            aria-hidden="true"
                        >
                            {formatAxisValue(mid)}
                        </text>
                        <text
                            x={width - padding + 5}
                            y={padding + (height - 2 * padding) * 0.75}
                            textAnchor="start"
                            fill="#9CA3AF"
                            fontSize="10"
                            aria-hidden="true"
                        >
                            {formatAxisValue(quarter3)}
                        </text>
                        <text
                            x={width - padding + 5}
                            y={height - padding}
                            textAnchor="start"
                            fill="#9CA3AF"
                            fontSize="10"
                            aria-hidden="true"
                        >
                            {formatAxisValue(min)}
                        </text>

                        {/* x-axis labels */}
                        {timeLabels.map((label, index) => {
                            const xPos = padding + (index / (timeLabels.length - 1)) * (width - 2 * padding);
                            return (
                                <text
                                    key={index}
                                    x={xPos}
                                    y={height - padding + 15}
                                    textAnchor="middle"
                                    fill="#9CA3AF"
                                    fontSize="10"
                                    aria-hidden="true"
                                >
                                    {label}
                                </text>
                            );
                        })}

                        {/* axis titles */}
                        <text
                            x={width - padding + 55}
                            y={height / 2}
                            textAnchor="middle"
                            fill="#9CA3AF"
                            fontSize="12"
                            transform={`rotate(-90 ${width - padding + 55} ${height / 2})`}
                            aria-hidden="true"
                        >
                            Price (USD)
                        </text>
                        <text
                            x={width / 2}
                            y={height - 10}
                            textAnchor="middle"
                            fill="#9CA3AF"
                            fontSize="12"
                            aria-hidden="true"
                        >
                            Time
                        </text>
                    </>
                )}

                {/* grid */}
                {showAxes && (
                    <>
                        {/* horizontal grid lines */}
                        <line
                            x1={padding}
                            y1={padding}
                            x2={width - padding}
                            y2={padding}
                            stroke="#374151"
                            strokeWidth="0.5"
                            strokeDasharray="2,2"
                            aria-hidden="true"
                        />
                        <line
                            x1={padding}
                            y1={padding + (height - 2 * padding) * 0.25}
                            x2={width - padding}
                            y2={padding + (height - 2 * padding) * 0.25}
                            stroke="#374151"
                            strokeWidth="0.5"
                            strokeDasharray="2,2"
                            aria-hidden="true"
                        />
                        <line
                            x1={padding}
                            y1={height / 2}
                            x2={width - padding}
                            y2={height / 2}
                            stroke="#374151"
                            strokeWidth="0.5"
                            strokeDasharray="2,2"
                            aria-hidden="true"
                        />
                        <line
                            x1={padding}
                            y1={padding + (height - 2 * padding) * 0.75}
                            x2={width - padding}
                            y2={padding + (height - 2 * padding) * 0.75}
                            stroke="#374151"
                            strokeWidth="0.5"
                            strokeDasharray="2,2"
                            aria-hidden="true"
                        />
                        <line
                            x1={padding}
                            y1={height - padding}
                            x2={width - padding}
                            y2={height - padding}
                            stroke="#374151"
                            strokeWidth="0.5"
                            strokeDasharray="2,2"
                            aria-hidden="true"
                        />

                        {/* vertical grid lines */}
                        {timeLabels.map((_, index) => {
                            const xPos = padding + (index / (timeLabels.length - 1)) * (width - 2 * padding);
                            return (
                                <line
                                    key={index}
                                    x1={xPos}
                                    y1={padding}
                                    x2={xPos}
                                    y2={height - padding}
                                    stroke="#374151"
                                    strokeWidth="0.5"
                                    strokeDasharray="2,2"
                                    aria-hidden="true"
                                />
                            );
                        })}
                    </>
                )}

                {/* area below the graph */}
                <polygon
                    points={`${padding},${height - padding} ${points} ${width - padding},${height - padding}`}
                    fill="url(#lineGradient)"
                    aria-hidden="true"
                />

                {/* graph line */}
                <polyline
                    points={points}
                    fill="none"
                    stroke={isPositive ? '#10B981' : '#EF4444'}
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                />

                {/* last dot */}
                {points && (
                    <circle
                        cx={points.split(' ').pop()?.split(',')[0]}
                        cy={points.split(' ').pop()?.split(',')[1]}
                        r="4"
                        fill={isPositive ? '#10B981' : '#EF4444'}
                        aria-hidden="true"
                    />
                )}
            </svg>
        </div>
    );
};