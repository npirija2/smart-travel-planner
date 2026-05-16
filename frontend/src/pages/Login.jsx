import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage('');

    try {
      await login(formData.email, formData.password);
      navigate('/planning');
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || 'We could not sign you in with those credentials.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <div className="auth-panel">
        <span className="eyebrow">Secure access</span>
        <h1>Welcome back.</h1>
        <p>Use your account to reach the full planning workspace without leaving the SPA flow.</p>
      </div>
      <div className="auth-card">
        <h2>Sign in</h2>
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              required
              type="email"
              value={formData.email}
              onChange={(event) => setFormData({ ...formData, email: event.target.value })}
            />
          </label>
          <label>
            Password
            <input
              required
              type="password"
              value={formData.password}
              onChange={(event) => setFormData({ ...formData, password: event.target.value })}
            />
          </label>
          {errorMessage && <div className="inline-error">{errorMessage}</div>}
          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="auth-switch">
          No account yet? <Link to="/register">Create one</Link>
        </p>
      </div>
    </section>
  );
}
