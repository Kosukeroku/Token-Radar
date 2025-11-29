interface ErrorDisplayProps {
    message: string;
    onRetry?: () => void;
}

export const ErrorDisplay: React.FC<ErrorDisplayProps> = ({ message, onRetry }) => {
    return (
        <div className="flex flex-col justify-center items-center h-64 space-y-4">
            <div className="text-xl text-red-500 text-center">{message}</div>
            {onRetry && (
                <button
                    onClick={onRetry}
                    className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
                >
                    Try Again
                </button>
            )}
        </div>
    )
}