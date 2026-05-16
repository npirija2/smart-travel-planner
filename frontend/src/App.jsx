import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Planning from './pages/Planning';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

function AppLayout() {
  const { currentUser, isAuthenticated, logout, loading } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link className="brand-mark" to="/">
          <span className="brand-orb" />
          <span className="brand-copy">
            <strong>Smart Travel Planner</strong>
            <span>Microservice-powered trip planning</span>
          </span>
        </Link>
        <nav className="topbar-nav">
          <Link to="/">Home</Link>
          {isAuthenticated && <Link to="/planning">Workspace</Link>}
          {!loading && !isAuthenticated && <Link to="/login">Login</Link>}
          {!loading && !isAuthenticated && <Link to="/register">Register</Link>}
        </nav>
        <div className="topbar-actions">
          {isAuthenticated && currentUser ? (
            <>
              <div className="session-pill">
                <span>{currentUser.username}</span>
                <small>{currentUser.email}</small>
              </div>
              <button
                className="secondary-button"
                onClick={() => {
                  logout();
                  navigate('/login');
                }}
                type="button"
              >
                Logout
              </button>
            </>
          ) : (
            <Link className="primary-link" to="/register">
              Start planning
            </Link>
          )}
        </div>
      </header>
      <main className="page-shell">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route
            path="/login"
            element={isAuthenticated ? <Navigate replace to="/planning" /> : <Login />}
          />
          <Route
            path="/register"
            element={isAuthenticated ? <Navigate replace to="/planning" /> : <Register />}
          />
          <Route
            path="/planning"
            element={
              <ProtectedRoute>
                <Planning />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </BrowserRouter>
  );
}
