export default function FeaturePreview({ feature }) {
  return (
    <div className="page-stack">
      <section className="hero-banner compact-hero">
        <div className="hero-copy">
          <p className="eyebrow">Prototype module</p>
          <h2>{feature.name}</h2>
          <p>{feature.summary}</p>
        </div>

        <div className="hero-panel small-panel">
          <div className="hero-metric">
            <strong>{feature.symbol}</strong>
            <span>module code</span>
          </div>
          <div className="hero-metric">
            <strong>{feature.status}</strong>
            <span>implementation state</span>
          </div>
        </div>
      </section>

      <section className="content-grid">
        <div className="section-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Frontend ready</p>
              <h3>Design translated from the reference ZIP</h3>
            </div>
          </div>

          <p className="body-copy">
            This screen now exists inside your current app shell so the larger product architecture from the
            ZIP can be navigated, reviewed, and incrementally connected to backend services later.
          </p>

          <div className="tag-row">
            {feature.highlights.map((highlight) => (
              <span key={highlight} className="tag-chip">
                {highlight}
              </span>
            ))}
          </div>
        </div>

        <aside className="section-card spotlight-card">
          <p className="eyebrow">Suggested next backend step</p>
          <h3>Wire this module to its own API slice</h3>
          <ul className="feature-list compact-list">
            <li>Create service methods in `frontend/src/api` for this feature.</li>
            <li>Replace placeholder insights with real backend responses.</li>
            <li>Keep the current shell and card patterns for consistency.</li>
          </ul>
        </aside>
      </section>
    </div>
  );
}
