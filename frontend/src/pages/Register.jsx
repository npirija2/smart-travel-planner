import React, { useState } from 'react';
import { registerUser } from '../api/userService';
import { useNavigate } from 'react-router-dom';

const Register = () => {
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await registerUser(formData);
            alert("Registration successful! You can now log in.");
            navigate('/login');
        } catch (error) {
            alert("Registration error: " + (error.response?.data || "Please check your data"));
        }
    };

    return (
        <div className="container" style={{ maxWidth: '450px' }}>
            <div className="auth-card">
                <h2 style={{ textAlign: 'center', color: '#007bff' }}>New Profile</h2>
                <p style={{ textAlign: 'center', color: '#666', marginBottom: '20px' }}>
                    Join the Smart Travel community.
                </p>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div className="form-group">
                        <label>Username</label>
                        <input 
                            type="text" 
                            placeholder="e.g. traveler123" 
                            required
                            onChange={(e) => setFormData({...formData, username: e.target.value})} 
                        />
                    </div>
                    <div className="form-group">
                        <label>Email</label>
                        <input 
                            type="email" 
                            placeholder="your@email.com" 
                            required
                            onChange={(e) => setFormData({...formData, email: e.target.value})} 
                        />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input 
                            type="password" 
                            placeholder="Min. 6 characters" 
                            required
                            minLength="6"
                            onChange={(e) => setFormData({...formData, password: e.target.value})} 
                        />
                    </div>
                    <button type="submit" className="btn-primary" style={{ padding: '12px', marginTop: '10px' }}>
                        Create Profile
                    </button>
                </form>
                <p style={{ textAlign: 'center', marginTop: '20px', fontSize: '0.9rem' }}>
                    Already have a profile? <a href="/login" style={{ color: '#007bff', textDecoration: 'none' }}>Login here</a>
                </p>
            </div>
        </div>
    );
};

export default Register;