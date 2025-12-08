import { Component } from 'react';
import type { ErrorInfo, ReactNode } from 'react';

interface ErrorBoundaryProps {
    children: ReactNode;
    fallback?: ReactNode;
}

interface ErrorBoundaryState {
    hasError: boolean;
    error: Error | null;
    errorInfo: ErrorInfo | null;
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
    constructor(props: ErrorBoundaryProps) {
        super(props);
        this.state = {
            hasError: false,
            error: null,
            errorInfo: null
        };
    }

    static getDerivedStateFromError(error: Error): ErrorBoundaryState {
        return {
            hasError: true,
            error,
            errorInfo: null
        };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
        this.setState({
            error,
            errorInfo
        });

        console.error('ErrorBoundary caught an error:', error, errorInfo);
    }

    resetErrorBoundary = (): void => {
        this.setState({
            hasError: false,
            error: null,
            errorInfo: null
        });
    };

    render(): ReactNode {
        if (this.state.hasError) {
            // custom fallback UI
            if (this.props.fallback) {
                return this.props.fallback;
            }

            // default fallback
            return (
                <div className="min-h-screen bg-gray-900 text-white flex items-center justify-center p-4">
                    <div className="bg-gray-800 rounded-xl p-8 max-w-lg w-full">
                        <div className="text-center mb-6">
                            <div className="text-6xl mb-4">⚠️</div>
                            <h1 className="text-2xl font-bold text-red-400 mb-2">Something went wrong</h1>
                            <p className="text-gray-400 mb-6">
                                The application encountered an unexpected error. Please try refreshing the page.
                            </p>
                        </div>

                        <div className="mb-6 p-4 bg-gray-900/50 rounded-lg">
                            <p className="text-sm text-gray-300 mb-2">Error details:</p>
                            <pre className="text-xs text-red-300 overflow-auto max-h-32">
                {this.state.error?.toString()}
              </pre>
                            {this.state.errorInfo && (
                                <pre className="text-xs text-gray-400 overflow-auto max-h-48 mt-2">
                  {this.state.errorInfo.componentStack}
                </pre>
                            )}
                        </div>

                        <div className="flex flex-col sm:flex-row gap-3">
                            <button
                                onClick={this.resetErrorBoundary}
                                className="flex-1 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors"
                            >
                                Try again
                            </button>
                            <button
                                onClick={() => window.location.reload()}
                                className="flex-1 py-3 bg-gray-700 hover:bg-gray-600 text-white font-semibold rounded-lg transition-colors"
                            >
                                Refresh page
                            </button>
                            <button
                                onClick={() => window.location.href = '/'}
                                className="flex-1 py-3 bg-gray-800 hover:bg-gray-700 text-white font-semibold rounded-lg transition-colors"
                            >
                                Go to homepage
                            </button>
                        </div>

                        <div className="mt-6 text-center">
                            <p className="text-sm text-gray-500">
                                If the problem persists, please contact support.
                            </p>
                        </div>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;