import React, { useMemo, useState } from 'react';
import { loginUser } from '../api/userService';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [submitting, setSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const registered = searchParams.get('registered') === '1';
    const redirectPath = useMemo(() => location.state?.from || '/planning', [location.state]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            setSubmitting(true);
            setErrorMessage('');
            const authData = await loginUser(formData.email, formData.password);
            localStorage.setItem('token', authData.accessToken);
            if (authData.refreshToken) {
                localStorage.setItem('refreshToken', authData.refreshToken);
            }
            window.dispatchEvent(new Event('auth-change'));
            navigate(redirectPath, { replace: true });
        } catch (error) {
            setErrorMessage(error.response?.data || 'Invalid email or password.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-shell">
            <section className="auth-panel auth-aside">
                <p className="eyebrow">Smart Travel Planner</p>
                <h1>Pick up every trip right where you left it.</h1>
                <p>
                    The new frontend now reflects the module-rich planner from your ZIP reference, and this login
                    flow takes you straight into that workspace.
                </p>

                <ul className="feature-list">
                    <li>Protected planning workspace with live backend data.</li>
                    <li>Dashboard shell for route, weather, budget, and collaboration modules.</li>
                    <li>Consistent travel UI across auth, overview, and itinerary creation.</li>
                </ul>
            </section>

            <section className="auth-panel auth-card">
                <div className="auth-card-header">
                    <p className="eyebrow">Welcome back</p>
                    <h2>Login</h2>
                    <p>Enter your credentials to access saved plans and destination data.</p>
                </div>

                {registered ? (
                    <div className="feedback-panel feedback-success">
                        <strong>Account created</strong>
                        <p>You can log in now and continue into the planning workspace.</p>
                    </div>
                ) : null}

                {errorMessage ? (
                    <div className="feedback-panel feedback-error">
                        <strong>Login failed</strong>
                        <p>{errorMessage}</p>
                    </div>
                ) : null}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="login-email">Email address</label>
                        <input
                            id="login-email"
                            type="email"
                            placeholder="name@example.com"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="login-password">Password</label>
                        <input
                            id="login-password"
                            type="password"
                            placeholder="••••••••"
                            required
                            value={formData.password}
                            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        />
                    </div>

                    <button type="submit" className="primary-button auth-submit" disabled={submitting}>
                        {submitting ? 'Signing in...' : 'Login'}
                    </button>
                </form>

                <p className="auth-footer">
                    Don&apos;t have an account yet? <Link to="/register">Register here</Link>
                </p>
            </section>
        </div>
    );
};

export default Login;
