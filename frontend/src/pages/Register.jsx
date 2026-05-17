import React, { useState } from 'react';
import { registerUser } from '../api/userService';
import { Link, useNavigate } from 'react-router-dom';

const Register = () => {
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            setSubmitting(true);
            setErrorMessage('');
            await registerUser(formData);
            navigate('/login?registered=1');
        } catch (error) {
            setErrorMessage(error.response?.data || 'Please check your data and try again.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-shell">
            <section className="auth-panel auth-aside">
                <p className="eyebrow">New workspace access</p>
                <h1>Create your travel planning profile.</h1>
                <p>
                    This updated frontend is built from the structure of the provided ZIP, so registration now leads
                    into a larger trip-planning workspace instead of a single standalone page.
                </p>

                <ul className="feature-list">
                    <li>Central dashboard for every travel-planning module.</li>
                    <li>Connected itinerary creation with live destination data.</li>
                    <li>Room to expand into weather, budget, collaboration, and sharing flows.</li>
                </ul>
            </section>

            <section className="auth-panel auth-card">
                <div className="auth-card-header">
                    <p className="eyebrow">Join the planner</p>
                    <h2>Create account</h2>
                    <p>Set up a profile to start building travel plans in the new frontend workspace.</p>
                </div>

                {errorMessage ? (
                    <div className="feedback-panel feedback-error">
                        <strong>Registration failed</strong>
                        <p>{errorMessage}</p>
                    </div>
                ) : null}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="register-username">Username</label>
                        <input
                            id="register-username"
                            type="text"
                            placeholder="e.g. traveler123"
                            required
                            value={formData.username}
                            onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="register-email">Email</label>
                        <input
                            id="register-email"
                            type="email"
                            placeholder="your@email.com"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="register-password">Password</label>
                        <input
                            id="register-password"
                            type="password"
                            placeholder="Minimum 6 characters"
                            required
                            minLength="6"
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        />
                    </div>

                    <button type="submit" className="primary-button auth-submit" disabled={submitting}>
                        {submitting ? 'Creating account...' : 'Create profile'}
                    </button>
                </form>

                <p className="auth-footer">
                    Already registered? <Link to="/login">Login here</Link>
                </p>
            </section>
        </div>
    );
};

export default Register;
