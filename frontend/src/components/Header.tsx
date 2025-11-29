const Header: React.FC = () => {
    return (
        <header className="bg-gray-800 border-b border-gray-700 py-2 px-4">
            <div className="flex items-center space-x-2">
                <img
                    src="/logo.png"
                    alt="Token Radar Logo"
                    className="w-10 h-10"
                />
                <h1 className="text-2xl font-bold text-white font-serif italic">Token Radar</h1>
            </div>
        </header>
    );
};

export default Header;