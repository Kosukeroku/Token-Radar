import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import CoinDetail from './components/CoinDetail';
import Header from './components/Header';
import './index.css';
import Login from "./components/Login.tsx";
import Register from "./components/Register.tsx";

function App() {
    return (
        <Router>
            <div className="min-h-screen bg-gray-900">
                <Header />
                <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/coin/:coinId" element={<CoinDetail />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;