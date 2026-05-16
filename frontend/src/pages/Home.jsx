import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const capabilities = [
  'Create travel plans and generate day-by-day itineraries.',
  'Schedule activities into morning, noon, and evening slots.',
  'Track budgets, expenses, reservations, and saga reservation status.',
  'Collaborate through shared links, membership management, and activity voting.',
  'Receive notifications, keep offline snapshots, and export the trip as PDF.',
];

export default function Home() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="home-page">
      <section className="hero-panel">
        <div className="hero-copy">
          <span className="eyebrow">Single-page travel workspace</span>
          <h1>Plan, coordinate, budget, and share every trip in one secure interface.</h1>
          <p>
            Smart Travel Planner connects your microservice backend to a modern SPA flow with
            authentication, itinerary management, collaboration, and finance tracking.
          </p>
          <div className="hero-actions">
            {isAuthenticated ? (
              <Link className="primary-link" to="/planning">
                Open workspace
              </Link>
            ) : (
              <>
                <Link className="primary-link" to="/register">
                  Create an account
                </Link>
                <Link className="secondary-link" to="/login">
                  Sign in
                </Link>
              </>
            )}
          </div>
        </div>
        <div className="hero-card-grid">
          {capabilities.map((capability) => (
            <article className="feature-card" key={capability}>
              <h3>Connected workflow</h3>
              <p>{capability}</p>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
