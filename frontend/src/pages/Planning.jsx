import React, { useEffect, useMemo, useState } from 'react';
import { getTravelPlans, createTravelPlan, getTravelPlanDays } from '../api/planService';
import { getDestinations } from '../api/destinationService';
import { Link } from 'react-router-dom';

const dateFormatter = new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
});

const getDurationLabel = (startDate, endDate) => {
    if (!startDate || !endDate) {
        return 'Choose your travel dates to preview trip length.';
    }

    const start = new Date(startDate);
    const end = new Date(endDate);
    const millisecondsPerDay = 1000 * 60 * 60 * 24;
    const difference = Math.round((end - start) / millisecondsPerDay) + 1;

    if (Number.isNaN(difference) || difference <= 0) {
        return 'End date should be on or after the start date.';
    }

    return `${difference} day${difference === 1 ? '' : 's'} planned`;
};

const formatDateRange = (startDate, endDate) => {
    if (!startDate || !endDate) {
        return 'Dates to be confirmed';
    }

    return `${dateFormatter.format(new Date(startDate))} to ${dateFormatter.format(new Date(endDate))}`;
};

const Planning = () => {
    const [plans, setPlans] = useState([]);
    const [destinations, setDestinations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [feedback, setFeedback] = useState({ type: '', message: '' });
    const [selectedPlanId, setSelectedPlanId] = useState('');
    const [selectedPlanDays, setSelectedPlanDays] = useState([]);
    const [formData, setFormData] = useState({
        name: '',
        startDate: '',
        endDate: '',
        destinationId: '',
        description: '',
        status: 'PENDING'
    });

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        try {
            setLoading(true);
            const [plansData, destData] = await Promise.all([
                getTravelPlans(),
                getDestinations()
            ]);
            const plansList = Array.isArray(plansData) ? plansData : [];
            const destinationList = Array.isArray(destData) ? destData : [];

            setPlans(plansList);
            setDestinations(destinationList);
            if (plansList.length > 0) {
                setSelectedPlanId((current) => current || String(plansList[0].id));
            }
            
            if (destinationList.length > 0) {
                setFormData((prev) => ({
                    ...prev,
                    destinationId: prev.destinationId || String(destinationList[0].id),
                }));
            }
        } catch (err) {
            console.error("Error loading data:", err);
            setFeedback({
                type: 'error',
                message: 'We could not load plans or destinations right now. Check your backend connection and try again.',
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!selectedPlanId) {
            setSelectedPlanDays([]);
            return;
        }

        getTravelPlanDays(selectedPlanId)
            .then((days) => setSelectedPlanDays(Array.isArray(days) ? days : []))
            .catch((error) => {
                console.error('Could not load generated plan days:', error);
                setSelectedPlanDays([]);
            });
    }, [selectedPlanId]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.endDate && formData.startDate && formData.endDate < formData.startDate) {
            setFeedback({
                type: 'error',
                message: 'End date must be on or after the start date.',
            });
            return;
        }

        try {
            setSubmitting(true);
            setFeedback({ type: '', message: '' });
            await createTravelPlan(formData);
            const nextDestinationId = formData.destinationId || (destinations[0] ? String(destinations[0].id) : '');
            setFormData({
                name: '',
                startDate: '',
                endDate: '',
                destinationId: nextDestinationId,
                description: '',
                status: 'PENDING'
            });
            const updatedPlans = await getTravelPlans();
            const normalizedPlans = Array.isArray(updatedPlans) ? updatedPlans : [];
            setPlans(normalizedPlans);
            if (normalizedPlans.length > 0) {
                setSelectedPlanId(String(normalizedPlans[normalizedPlans.length - 1].id));
            }
            setFeedback({
                type: 'success',
                message: 'Travel plan created successfully. Your itinerary is now listed below.',
            });
        } catch (err) {
            setFeedback({
                type: 'error',
                message: err.response?.data?.message || 'The plan could not be saved. Check the backend connection and try again.',
            });
        } finally {
            setSubmitting(false);
        }
    };

    const selectedDestination = useMemo(
        () => destinations.find((destination) => String(destination.id) === String(formData.destinationId)),
        [destinations, formData.destinationId]
    );

    const approvedPlans = plans.filter((plan) => plan.status === 'APPROVED').length;
    const pendingPlans = plans.filter((plan) => plan.status === 'PENDING').length;

    if (loading) {
        return (
            <div className="page-stack">
                <section className="section-card empty-state">
                    <h3>Loading planning workspace</h3>
                    <p>Fetching destinations and saved trips from your backend services.</p>
                </section>
            </div>
        );
    }

    return (
        <div className="page-stack planning-page">
            <section className="hero-banner compact-hero">
                <div className="hero-copy">
                    <p className="eyebrow">Live itinerary builder</p>
                    <h2>Create travel plans with the ZIP-inspired workflow.</h2>
                    <p>
                        This page now follows the richer create-plan layout from your reference frontend while staying
                        fully connected to the existing `travel-plans` and `destinations` endpoints.
                    </p>
                </div>

                <div className="hero-panel small-panel">
                    <div className="hero-metric">
                        <strong>{plans.length}</strong>
                        <span>saved plans</span>
                    </div>
                    <div className="hero-metric">
                        <strong>{destinations.length}</strong>
                        <span>destinations</span>
                    </div>
                    <div className="hero-metric">
                        <strong>{pendingPlans}</strong>
                        <span>pending review</span>
                    </div>
                </div>
            </section>

            {feedback.message ? (
                <section className={`feedback-panel ${feedback.type === 'success' ? 'feedback-success' : 'feedback-error'}`}>
                    <strong>{feedback.type === 'success' ? 'Plan saved' : 'Action needed'}</strong>
                    <p>{feedback.message}</p>
                </section>
            ) : null}

            <div className="planning-grid">
                <section className="section-card">
                    <div className="section-heading">
                        <div>
                            <p className="eyebrow">Create trip</p>
                            <h3>Plan setup</h3>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="planning-form">
                        <div className="form-group full-span">
                            <label htmlFor="plan-name">Plan name</label>
                            <input
                                id="plan-name"
                                type="text"
                                placeholder="e.g. Summer Vacation 2026"
                                required
                                value={formData.name}
                                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="plan-destination">Destination</label>
                            <select
                                id="plan-destination"
                                value={formData.destinationId}
                                onChange={(e) => setFormData({ ...formData, destinationId: e.target.value })}
                                required
                            >
                                <option value="">Select a destination...</option>
                                {destinations.map((destination) => (
                                    <option key={destination.id} value={destination.id}>
                                        {destination.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="plan-status">Initial status</label>
                            <select
                                id="plan-status"
                                value={formData.status}
                                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                            >
                                <option value="PENDING">PENDING</option>
                                <option value="APPROVED">APPROVED</option>
                                <option value="REJECTED">REJECTED</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label htmlFor="plan-start">Start date</label>
                            <input
                                id="plan-start"
                                type="date"
                                required
                                value={formData.startDate}
                                onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="plan-end">End date</label>
                            <input
                                id="plan-end"
                                type="date"
                                required
                                value={formData.endDate}
                                onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
                            />
                        </div>

                        <div className="form-group full-span">
                            <label htmlFor="plan-description">Additional notes</label>
                            <textarea
                                id="plan-description"
                                placeholder="What are you planning to visit, coordinate, or book?"
                                value={formData.description}
                                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                rows="5"
                            />
                        </div>

                        <div className="form-actions full-span">
                            <button type="submit" className="primary-button" disabled={submitting}>
                                {submitting ? 'Saving plan...' : 'Save travel plan'}
                            </button>
                        </div>
                    </form>
                </section>

                <aside className="planning-sidebar">
                    <section className="section-card spotlight-card">
                        <p className="eyebrow">Trip preview</p>
                        <h3>{formData.name || 'Your next itinerary'}</h3>
                        <p>{selectedDestination?.name || 'Select a destination to preview the trip context.'}</p>

                        <div className="summary-stack">
                            <div className="summary-row">
                                <span>Dates</span>
                                <strong>{formatDateRange(formData.startDate, formData.endDate)}</strong>
                            </div>
                            <div className="summary-row">
                                <span>Duration</span>
                                <strong>{getDurationLabel(formData.startDate, formData.endDate)}</strong>
                            </div>
                            <div className="summary-row">
                                <span>Status</span>
                                <strong>{formData.status}</strong>
                            </div>
                        </div>
                    </section>

                    <section className="section-card">
                        <p className="eyebrow">Workspace snapshot</p>
                        <div className="mini-stat-grid">
                            <div className="mini-stat">
                                <strong>{approvedPlans}</strong>
                                <span>approved</span>
                            </div>
                            <div className="mini-stat">
                                <strong>{pendingPlans}</strong>
                                <span>pending</span>
                            </div>
                            <div className="mini-stat">
                                <strong>{destinations.length}</strong>
                                <span>destinations</span>
                            </div>
                        </div>
                    </section>

                    <section className="section-card">
                        <p className="eyebrow">Planning checklist</p>
                        <ul className="feature-list compact-list">
                            <li>Name the trip clearly so it stands out in the dashboard.</li>
                            <li>Set realistic dates before connecting route and weather modules later.</li>
                            <li>Use notes for booking context, must-see places, or approval details.</li>
                        </ul>
                    </section>
                </aside>
            </div>

            <section className="section-card">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Saved itineraries</p>
                        <h3>Active plans</h3>
                    </div>
                </div>

                {plans.length === 0 ? (
                    <div className="empty-state">
                        <h4>No travel plans created yet</h4>
                        <p>Save your first itinerary above and it will appear here instantly.</p>
                    </div>
                ) : (
                    <div className="plan-table-wrapper">
                        <table className="plan-table">
                            <thead>
                                <tr>
                                    <th>Name</th>
                                    <th>Destination</th>
                                    <th>Period</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {plans.map((plan) => (
                                    <tr key={plan.id}>
                                        <td>
                                            <strong>{plan.name}</strong>
                                        </td>
                                        <td>{plan.destinationName || 'Not specified'}</td>
                                        <td>{formatDateRange(plan.startDate, plan.endDate)}</td>
                                        <td>
                                            <span className={`status-badge status-${String(plan.status || 'draft').toLowerCase()}`}>
                                                {plan.status || 'DRAFT'}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            <section className="section-card">
                <div className="section-heading">
                    <div>
                        <p className="eyebrow">Generated day structure</p>
                        <h3>Daily itinerary scaffold</h3>
                    </div>
                    <div className="module-toolbar">
                        <select value={selectedPlanId} onChange={(event) => setSelectedPlanId(event.target.value)}>
                            <option value="">Select a plan...</option>
                            {plans.map((plan) => (
                                <option key={plan.id} value={plan.id}>
                                    {plan.name}
                                </option>
                            ))}
                        </select>
                        <Link to="/activity-scheduling" className="ghost-button">
                            Edit activities
                        </Link>
                    </div>
                </div>

                {selectedPlanDays.length ? (
                    <div className="stack-list">
                        {selectedPlanDays.map((day) => (
                            <article key={day.id} className="trip-row">
                                <div>
                                    <strong>{formatDateRange(day.date, day.date)}</strong>
                                    <p>{day.activities?.length || 0} scheduled activities</p>
                                </div>
                                <div className="trip-row-meta">
                                    <span className="tag-chip">
                                        {day.activities?.length ? 'Ready for editing' : 'Empty day'}
                                    </span>
                                </div>
                            </article>
                        ))}
                    </div>
                ) : (
                    <div className="empty-state compact-empty">
                        <h4>No day structure loaded</h4>
                        <p>Create a plan or select another one to inspect its generated daily structure.</p>
                    </div>
                )}
            </section>
        </div>
    );
};

export default Planning;
