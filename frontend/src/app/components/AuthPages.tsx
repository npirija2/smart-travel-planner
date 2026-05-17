import { LogIn, MapPinned, UserPlus } from "lucide-react";
import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { getApiErrorMessage } from "../../api/errorUtils";
import { useAuth } from "../context/AuthContext";

function AuthShell({ eyebrow, title, description, icon: Icon, children, footer }) {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-6 py-10">
      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-[1.1fr_0.9fr] gap-6">
        <section className="bg-white border-2 border-gray-300 rounded-lg p-8">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center mb-5">
            <Icon className="w-6 h-6 text-blue-600" />
          </div>
          <p className="text-sm font-medium text-gray-600 mb-2">{eyebrow}</p>
          <h1 className="text-3xl font-medium mb-3">{title}</h1>
          <p className="text-gray-600 mb-6">{description}</p>
          <div className="space-y-3 text-sm text-gray-700">
            <div className="border border-gray-300 rounded p-4 bg-gray-50">Plan trips, organize details, and keep everything in one place.</div>
            <div className="border border-gray-300 rounded p-4 bg-gray-50">Track itineraries, reservations, budgets, and travel updates.</div>
            <div className="border border-gray-300 rounded p-4 bg-gray-50">Pick up your travel plans anytime and continue where you left off.</div>
          </div>
        </section>

        <section className="bg-white border-2 border-gray-300 rounded-lg p-8">
          {children}
          <p className="mt-6 text-sm text-gray-600">{footer}</p>
        </section>
      </div>
    </div>
  );
}

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const registered = searchParams.get("registered") === "1";

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const redirectPath = location.state?.from || "/";

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      setSubmitting(true);
      setErrorMessage("");
      await login(formData);
      navigate(redirectPath, { replace: true });
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, "Login failed. Please check your credentials."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      eyebrow="Workspace access"
      title="Sign in to your travel planner."
      description="Access your travel plans, trip details, and planning tools in one organized workspace."
      icon={LogIn}
      footer={<span>Need an account? <Link className="text-blue-600 hover:underline" to="/register">Create one here</Link>.</span>}
    >
      <p className="text-sm font-medium text-gray-600 mb-2">Welcome back</p>
      <h2 className="text-2xl font-medium mb-2">Login</h2>
      <p className="text-sm text-gray-600 mb-6">Sign in to access your saved trips, schedules, reservations, and planning tools.</p>

      {registered ? (
        <div className="bg-green-50 border border-green-300 rounded p-4 text-sm text-green-800 mb-4">
          Registration successful. You can sign in now.
        </div>
      ) : null}

      {errorMessage ? (
        <div className="bg-red-50 border border-red-300 rounded p-4 text-sm text-red-800 mb-4">
          {errorMessage}
        </div>
      ) : null}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-2">Email</label>
          <input
            type="email"
            required
            value={formData.email}
            onChange={(event) => setFormData({ ...formData, email: event.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            placeholder="name@example.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Password</label>
          <input
            type="password"
            required
            value={formData.password}
            onChange={(event) => setFormData({ ...formData, password: event.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            placeholder="••••••••"
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full px-6 py-3 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 disabled:opacity-70"
        >
          {submitting ? "Signing in..." : "Login"}
        </button>
      </form>
    </AuthShell>
  );
}

export function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ username: "", email: "", password: "" });
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      setSubmitting(true);
      setErrorMessage("");
      await register(formData);
      navigate("/login?registered=1", { replace: true });
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error, "Registration failed. Please review your details."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthShell
      eyebrow="New account"
      title="Create your travel planner profile."
      description="Create your account to start building trips, organizing itineraries, and managing travel plans with ease."
      icon={UserPlus}
      footer={<span>Already registered? <Link className="text-blue-600 hover:underline" to="/login">Sign in here</Link>.</span>}
    >
      <div className="flex items-center gap-2 text-sm text-gray-600 mb-2">
        <MapPinned className="w-4 h-4" />
        <span>Create your travel account</span>
      </div>
      <h2 className="text-2xl font-medium mb-2">Register</h2>
      <p className="text-sm text-gray-600 mb-6">Create your account to start building itineraries, tracking plans, and organizing every trip in one place.</p>

      {errorMessage ? (
        <div className="bg-red-50 border border-red-300 rounded p-4 text-sm text-red-800 mb-4">
          {errorMessage}
        </div>
      ) : null}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-2">Username</label>
          <input
            type="text"
            required
            value={formData.username}
            onChange={(event) => setFormData({ ...formData, username: event.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            placeholder="traveler123"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Email</label>
          <input
            type="email"
            required
            value={formData.email}
            onChange={(event) => setFormData({ ...formData, email: event.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            placeholder="name@example.com"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-2">Password</label>
          <input
            type="password"
            minLength={6}
            required
            value={formData.password}
            onChange={(event) => setFormData({ ...formData, password: event.target.value })}
            className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
            placeholder="Minimum 6 characters"
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full px-6 py-3 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 disabled:opacity-70"
        >
          {submitting ? "Creating account..." : "Create account"}
        </button>
      </form>
    </AuthShell>
  );
}
