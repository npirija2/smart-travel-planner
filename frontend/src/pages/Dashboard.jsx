import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getTravelPlans } from '../api/planService';
import { getDestinations } from '../api/destinationService';
import { features } from '../data/features';

export default function Dashboard({ isLoggedIn }) {
  const [plans, setPlans] = useState([]);
  const [destinations, setDestinations] = useState([]);
  const [loading, setLoading] = useState(isLoggedIn);
  const [loadError, setLoadError] = useState('');

  useEffect(() => {
    if (!isLoggedIn) {
      setPlans([]);
      setDestinations([]);
      setLoading(false);
      setLoadError('');
      return;
    }

    const loadDashboardData = async () => {
      try {
        setLoading(true);
        setLoadError('');

        const [plansData, destinationsData] = await Promise.all([
          getTravelPlans(),
          getDestinations(),
        ]);

        setPlans(Array.isArray(plansData) ? plansData : []);
        setDestinations(Array.isArray(destinationsData) ? destinationsData : []);
      } catch (error) {
        console.error('Dashboard data loading failed:', error);
        setLoadError('The dashboard layout is ready, but trip data could not be loaded right now.');
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, [isLoggedIn]);

  const liveFeatures = features.filter((feature) => feature.status === 'Live').length;
  const prototypeFeatures = features.length - liveFeatures;
  const activePlans = plans.filter((plan) => plan.status === 'APPROVED' || plan.status === 'PENDING').length;

  return (
    <div className="page-stack">
      <section className="hero-banner">
        <div className="hero-copy">
          <p className="eyebrow">Frontend implementation from your ZIP reference</p>
          <h2>Turn the planner into a full travel command center.</h2>
          <p>
            This dashboard mirrors the multi-module structure from the provided files while staying connected
            to your current login and travel-plan APIs.
          </p>

          <div className="hero-actions">
            <Link to={isLoggedIn ? '/planning' : '/register'} className="primary-button">
              {isLoggedIn ? 'Open planning workspace' : 'Create your account'}
            </Link>
            <Link to={isLoggedIn ? '/weather' : '/login'} className="ghost-button">
              {isLoggedIn ? 'Browse prototype modules' : 'Login to continue'}
            </Link>
          </div>
        </div>

        <div className="hero-panel">
          <div className="hero-metric">
            <strong>{loading ? '...' : plans.length}</strong>
            <span>saved trips</span>
          </div>
          <div className="hero-metric">
            <strong>{loading ? '...' : destinations.length}</strong>
            <span>available destinations</span>
          </div>
          <div className="hero-metric">
            <strong>{features.length}</strong>
            <span>frontend modules</span>
          </div>
        </div>
      </section>

      <section className="stat-grid">
        <article className="section-card stat-card">
          <p className="eyebrow">Trips in motion</p>
          <h3>{loading ? 'Loading...' : activePlans}</h3>
          <p>Plans that are currently pending or approved in the backend.</p>
        </article>

        <article className="section-card stat-card">
          <p className="eyebrow">Live module</p>
          <h3>{liveFeatures}</h3>
          <p>The planning workflow is fully wired to your existing services.</p>
        </article>

        <article className="section-card stat-card">
          <p className="eyebrow">Prototype modules</p>
          <h3>{prototypeFeatures}</h3>
          <p>Additional screens from the reference frontend are now represented and ready for backend pairing.</p>
        </article>
      </section>

      {loadError ? (
        <section className="section-card feedback-panel feedback-warning">
          <strong>Data connection warning</strong>
          <p>{loadError}</p>
        </section>
      ) : null}

      <section className="content-grid">
        <div className="section-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Current plans</p>
              <h3>Your travel queue</h3>
            </div>
            <Link to={isLoggedIn ? '/planning' : '/login'} className="text-link">
              {isLoggedIn ? 'Manage plans' : 'Unlock planning'}
            </Link>
          </div>

          {isLoggedIn ? (
            loading ? (
              <div className="empty-state">
                <h4>Loading trip data</h4>
                <p>Fetching saved travel plans and destination options.</p>
              </div>
            ) : plans.length === 0 ? (
              <div className="empty-state">
                <h4>No travel plans yet</h4>
                <p>Create your first itinerary to populate the dashboard.</p>
              </div>
            ) : (
              <div className="trip-list">
                {plans.slice(0, 4).map((plan) => (
                  <article key={plan.id} className="trip-row">
                    <div>
                      <strong>{plan.name}</strong>
                      <p>{plan.destinationName || 'Destination pending selection'}</p>
                    </div>
                    <div className="trip-row-meta">
                      <span>{plan.startDate || 'TBD'} to {plan.endDate || 'TBD'}</span>
                      <span className={`status-badge status-${String(plan.status || 'draft').toLowerCase()}`}>
                        {plan.status || 'DRAFT'}
                      </span>
                    </div>
                  </article>
                ))}
              </div>
            )
          ) : (
            <div className="empty-state">
              <h4>Sign in to load live trip data</h4>
              <p>The dashboard shell is active, but saved plans and destinations require authentication.</p>
            </div>
          )}
        </div>

        <aside className="section-card spotlight-card">
          <p className="eyebrow">Reference merge</p>
          <h3>What changed in this implementation</h3>
          <ul className="feature-list compact-list">
            <li>Sidebar navigation and module structure now mirror the ZIP frontend.</li>
            <li>Dashboard, planning, and auth screens share one cohesive visual system.</li>
            <li>Placeholder modules are ready to be connected to future backend services.</li>
          </ul>
        </aside>
      </section>

      <section className="section-card">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Module map</p>
            <h3>Planner capabilities</h3>
          </div>
        </div>

        <div className="feature-grid">
          {features.map((feature) => (
            <Link key={feature.path} to={feature.path} className="feature-card">
              <div className="feature-card-header">
                <span className="nav-symbol large">{feature.symbol}</span>
                <span className={`status-pill ${feature.status === 'Live' ? 'status-live' : 'status-prototype'}`}>
                  {feature.status}
                </span>
              </div>
              <h4>{feature.name}</h4>
              <p>{feature.summary}</p>
              <div className="tag-row">
                {feature.highlights.map((highlight) => (
                  <span key={highlight} className="tag-chip">
                    {highlight}
                  </span>
                ))}
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
