import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { api } from '../services/api';

interface TrackButtonProps {
    coinId: string;
    coinName?: string;
    onTrackChange?: (isTracked: boolean) => void;
}

const TrackButton: React.FC<TrackButtonProps> = ({
                                                     coinId,
                                                     coinName = '',
                                                     onTrackChange
                                                 }) => {
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();
    const [isTracked, setIsTracked] = useState(false);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) {
            checkTrackStatus();
        }
    }, [coinId, isAuthenticated]);

    const checkTrackStatus = async () => {
        if (!isAuthenticated) return;

        try {
            const response = await api.get('/tracked-currencies');
            const isTracked = response.data.some((tc: any) => tc.coinId === coinId);
            setIsTracked(isTracked);
        } catch (err) {
            console.error('Failed to check track status:', err);
        }
    };

    const handleClick = async (e: React.MouseEvent) => {
        e.stopPropagation();
        setLoading(true);

        if (!isAuthenticated) {
            navigate('/login');
            return;
        }

        try {
            if (isTracked) {
                await api.delete(`/tracked-currencies/${coinId}`);
                setIsTracked(false);
                onTrackChange?.(false);
            } else {
                await api.post('/tracked-currencies', { coinId });
                setIsTracked(true);
                onTrackChange?.(true);
            }
        } catch (err: any) {
            console.error('Failed to toggle track:', err);
            const errorMsg = err.response?.data?.message || `Failed to ${isTracked ? 'untrack' : 'track'} ${coinName || 'coin'}`;
            alert(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <button
            onClick={handleClick}
            disabled={loading}
            className={`
        px-2 py-1 text-xs min-w-[55px]
        ${isTracked ? 'bg-red-600 hover:bg-red-700' : 'bg-blue-600 hover:bg-blue-700'}
        text-white rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed
        font-medium
      `}
            title={isTracked ? `Untrack ${coinName}` : `Track ${coinName}`}
        >
            {loading ? (
                <div className="animate-spin rounded-full h-3 w-3 border-b-2 border-white mx-auto"></div>
            ) : (
                isTracked ? 'Untrack' : 'Track'
            )}
        </button>
    );
};

export default TrackButton;