import React, { useState } from 'react';
import { loginUser } from '../api/userService';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const token = await loginUser(formData.email, formData.password);
            localStorage.setItem('token', token);
            window.dispatchEvent(new Event("storage"));
            alert("Login successful!");
            window.location.href = "/planning"; 
        } catch (error) {
            alert("Login error: " + (error.response?.data || "Invalid email or password"));
        }
    };

    return (
        <div className="container" style={{ maxWidth: '400px' }}>
            <div className="auth-card">
                <h2 style={{ textAlign: 'center', color: '#007bff' }}>Login</h2>
                <p style={{ textAlign: 'center', color: '#666', marginBottom: '20px' }}>
                    Enter your credentials to access your plans.
                </p>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div className="form-group">
                        <label>Email Address</label>
                        <input 
                            type="email" 
                            placeholder="name@example.com" 
                            required
                            onChange={(e) => setFormData({...formData, email: e.target.value})} 
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input 
                            type="password" 
                            placeholder="••••••••" 
                            required
                            onChange={(e) => setFormData({...formData, password: e.target.value})} 
                        />
                    </div>
                    <button type="submit" className="btn-primary" style={{ padding: '12px', marginTop: '10px' }}>
                        Login
                    </button>
                </form>
                <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.9rem' }}>
                    Don't have an account? <a href="/register" style={{ color: '#007bff', textDecoration: 'none' }}>Register here</a>
                </p>
            </div>
        </div>
    );
};

export default Login;