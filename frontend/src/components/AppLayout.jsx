import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { features, findFeatureByPath } from '../data/features';

const dashboardLink = {
  name: 'Dashboard',
  path: '/',
  symbol: 'DB',
  status: 'Live',
  summary: 'Overview of active trips, destinations, and the planning modules in this workspace.',
};

const navigation = [dashboardLink, ...features];

const supportLinks = [
  { label: 'Microservices-ready', value: 'Backend connected' },
  { label: 'Reference style', value: 'ZIP-inspired frontend' },
];

export default function AppLayout({ isLoggedIn, onLogout }) {
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const activeItem = findFeatureByPath(location.pathname) ?? dashboardLink;

  return (
    <div className="app-shell">
      <aside className={`sidebar ${sidebarOpen ? '' : 'sidebar-collapsed'}`}>
        <div className="sidebar-brand">
          <Link to="/" className="brand-lockup">
            <span className="brand-mark">ST</span>
            {sidebarOpen ? (
              <span>
                <strong>Smart Travel</strong>
                <small>Planner workspace</small>
              </span>
            ) : null}
          </Link>

          <button
            type="button"
            className="sidebar-toggle"
            onClick={() => setSidebarOpen((current) => !current)}
            aria-label={sidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
          >
            {sidebarOpen ? '<<' : '>>'}
          </button>
        </div>

        <nav className="sidebar-nav" aria-label="Travel modules">
          {navigation.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
              title={item.name}
            >
              <span className="nav-symbol">{item.symbol}</span>
              {sidebarOpen ? (
                <span className="nav-copy">
                  <strong>{item.name}</strong>
                  <small>{item.status}</small>
                </span>
              ) : null}
            </NavLink>
          ))}
        </nav>

        {sidebarOpen ? (
          <div className="sidebar-footer">
            {supportLinks.map((item) => (
              <div key={item.label} className="sidebar-note">
                <small>{item.label}</small>
                <strong>{item.value}</strong>
              </div>
            ))}
          </div>
        ) : null}
      </aside>

      <div className="shell-main">
        <header className="shell-header">
          <div>
            <p className="eyebrow">Travel orchestration</p>
            <h1>{activeItem.name}</h1>
            <p className="header-summary">{activeItem.summary}</p>
          </div>

          <div className="header-actions">
            <div className="trip-pill">
              <span className="trip-dot" />
              {isLoggedIn ? 'Workspace unlocked' : 'Sign in to start planning'}
            </div>

            {isLoggedIn ? (
              <button type="button" className="ghost-button" onClick={onLogout}>
                Logout
              </button>
            ) : (
              <>
                <Link to="/login" className="ghost-button">
                  Login
                </Link>
                <Link to="/register" className="primary-button">
                  Create account
                </Link>
              </>
            )}
          </div>
        </header>

        <main className="shell-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
