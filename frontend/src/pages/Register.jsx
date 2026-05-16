import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const navigate = useNavigate();
  const { login, register } = useAuth();
  const [formData, setFormData] = useState({ username: '', email: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage('');

    try {
      await register(formData);
      await login(formData.email, formData.password);
      navigate('/planning');
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || 'We could not create the account with the provided data.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="auth-page">
      <div className="auth-panel">
        <span className="eyebrow">Start your workspace</span>
        <h1>Build your travel command center.</h1>
        <p>
          Create an account to manage itineraries, budgets, collaboration, notifications, and
          offline-ready exports in one place.
        </p>
      </div>
      <div className="auth-card">
        <h2>Create account</h2>
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            Username
            <input
              minLength={3}
              required
              type="text"
              value={formData.username}
              onChange={(event) => setFormData({ ...formData, username: event.target.value })}
            />
          </label>
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
              minLength={6}
              required
              type="password"
              value={formData.password}
              onChange={(event) => setFormData({ ...formData, password: event.target.value })}
            />
          </label>
          {errorMessage && <div className="inline-error">{errorMessage}</div>}
          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="auth-switch">
          Already registered? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </section>
  );
}
