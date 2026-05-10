import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Planning from './pages/Planning';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css'; 

const Dashboard = () => (
    <div className="container" style={{ textAlign: 'center', marginTop: '50px' }}>
        <h1>🌍 Smart Travel Planner</h1>
        <p>Your personal assistant for travel organization in a microservices environment.</p>
    </div>
);

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem('token'));

    useEffect(() => {
        const checkLogin = () => {
            setIsLoggedIn(!!localStorage.getItem('token'));
        };
        window.addEventListener('storage', checkLogin);
        return () => window.removeEventListener('storage', checkLogin);
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        setIsLoggedIn(false);
        window.location.href = "/login"; 
    };

    return (
        <Router>
            <nav className="navbar">
                <div className="nav-brand">
                    <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>SmartTravel</Link>
                </div>
                <div className="nav-links">
                    <Link to="/">Dashboard</Link>
                    
                    {!isLoggedIn ? (
                        <>
                            <Link to="/login">Login</Link>
                            <Link to="/register" className="btn-primary">Register</Link>
                        </>
                    ) : (
                        <>
                            <Link to="/planning">Planning</Link>
                            <button onClick={handleLogout} className="logout-btn">
                                Logout
                            </button>
                        </>
                    )}
                </div>
            </nav>

            <div className="main-content">
                <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    
                    <Route 
                        path="/planning" 
                        element={
                            <ProtectedRoute>
                                <Planning />
                            </ProtectedRoute>
                        } 
                    />
                </Routes>
            </div>
        </Router>
    );
}

export default App;