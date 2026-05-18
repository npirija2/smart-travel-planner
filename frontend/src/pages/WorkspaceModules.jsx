import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getActivitiesByDay,
  createActivity,
  deleteActivity,
  updateActivity,
} from '../api/activityService';
import { getBudgetsByPlan, createBudget, updateBudget } from '../api/budgetService';
import { getCategories } from '../api/categoryService';
import { getDestinations } from '../api/destinationService';
import { getExpensesByPlan, createExpense, deleteExpense } from '../api/expenseService';
import { getLocationsByDestination, createLocation } from '../api/locationService';
import { getNotificationsByPlan, createNotification, deleteNotification } from '../api/notificationService';
import { getPlanMembershipsForUser, createPlanMembership, deletePlanMembership } from '../api/planUserService';
import {
  getAttractions,
  getLocalRecommendations,
  getRouteOptimization,
  getScheduleLoad,
  getTravelPlanDays,
  getTravelPlans,
  getWaitingTimeInsights,
  getWeatherForecast,
} from '../api/planService';
import { getReservationsForPlan, getPremiumReservations, createReservation, deleteReservation } from '../api/reservationService';
import { createReview, getReviewsByActivity } from '../api/reviewService';
import { createSharedLink, deleteSharedLink, getSharedLinksByPlan } from '../api/sharedLinkService';
import { getCurrentUser, getAllUsers } from '../api/userService';
import { createVote, deleteVote, getVotesByActivity } from '../api/voteService';
import { createUserPreference, getUserPreferences } from '../api/preferenceService';

const defaultFeedback = { type: '', message: '' };

const formatDate = (value) => {
  if (!value) {
    return 'TBD';
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date(value));
};

const formatDateTime = (value) => {
  if (!value) {
    return 'Not scheduled';
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
};

const toDateTimeLocal = (value) => {
  if (!value) {
    return '';
  }

  const date = new Date(value);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

const flattenActivities = (days = []) =>
  days.flatMap((day) => (day.activities || []).map((activity) => ({ ...activity, dayDate: day.date, dayId: day.id })));

function useWorkspaceBootstrap() {
  const [plans, setPlans] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError('');
        const [userData, planData] = await Promise.all([getCurrentUser(), getTravelPlans()]);
        setCurrentUser(userData);
        setPlans(Array.isArray(planData) ? planData : []);
      } catch (loadError) {
        console.error('Workspace bootstrap failed:', loadError);
        setError('We could not load your workspace context. Please verify the backend services are running.');
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  return { plans, currentUser, loading, error };
}

function ModuleShell({ title, description, loading, error, plans, selectedPlanId, setSelectedPlanId, children, aside }) {
  return (
    <div className="page-stack">
      <section className="hero-banner compact-hero">
        <div className="hero-copy">
          <p className="eyebrow">Connected module</p>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <div className="hero-panel small-panel">
          <div className="hero-metric">
            <strong>{plans.length}</strong>
            <span>available plans</span>
          </div>
        </div>
      </section>

      {error ? (
        <section className="feedback-panel feedback-error">
          <strong>Module unavailable</strong>
          <p>{error}</p>
        </section>
      ) : null}

      <div className="content-grid">
        <div className="section-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Plan context</p>
              <h3>Select travel plan</h3>
            </div>
          </div>

          <div className="module-toolbar">
            <select
              value={selectedPlanId}
              onChange={(event) => setSelectedPlanId(event.target.value)}
              disabled={loading || plans.length === 0}
            >
              <option value="">Choose a plan...</option>
              {plans.map((plan) => (
                <option key={plan.id} value={plan.id}>
                  {plan.name} · {plan.destinationName || 'Destination pending'}
                </option>
              ))}
            </select>

            <Link className="ghost-button" to="/planning">
              Manage base plan data
            </Link>
          </div>

          {loading ? (
            <div className="empty-state">
              <h4>Loading workspace</h4>
              <p>Preparing user and plan context for this module.</p>
            </div>
          ) : selectedPlanId ? (
            children
          ) : (
            <div className="empty-state">
              <h4>Select a travel plan</h4>
              <p>Choose one of your saved plans to activate this module.</p>
            </div>
          )}
        </div>

        <aside className="section-card spotlight-card">{aside}</aside>
      </div>
    </div>
  );
}

function useSelectedPlan(plans) {
  const [selectedPlanId, setSelectedPlanId] = useState('');

  useEffect(() => {
    if (!selectedPlanId && plans.length > 0) {
      setSelectedPlanId(String(plans[0].id));
    }
  }, [plans, selectedPlanId]);

  return [selectedPlanId, setSelectedPlanId];
}

export function RouteOptimizationModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (!selectedPlanId) {
      setResult(null);
      return;
    }

    getRouteOptimization(selectedPlanId)
      .then(setResult)
      .catch((loadError) => {
        console.error(loadError);
        setResult(null);
      });
  }, [selectedPlanId]);

  return (
    <ModuleShell
      title="Route Optimization"
      description="The backend proposes a visit order using saved activity locations, coordinates, and timeslot priority."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">How it works</p>
          <h3>Optimization source</h3>
          <p className="body-copy">
            This module is driven by the planning service and recalculates from real activities and locations.
          </p>
        </>
      }
    >
      {result?.stops?.length ? (
        <div className="stack-list">
          <div className="mini-stat-grid">
            <div className="mini-stat">
              <strong>{result.stops.length}</strong>
              <span>planned stops</span>
            </div>
            <div className="mini-stat">
              <strong>{result.totalDistanceScore}</strong>
              <span>distance score</span>
            </div>
            <div className="mini-stat">
              <strong>{result.destinationName}</strong>
              <span>destination</span>
            </div>
          </div>

          {result.stops.map((stop) => (
            <article key={stop.activityId} className="trip-row">
              <div>
                <strong>
                  {stop.suggestedOrder}. {stop.activityName}
                </strong>
                <p>
                  {stop.locationName} · {stop.address}
                </p>
              </div>
              <div className="trip-row-meta">
                <span>{formatDate(stop.dayDate)}</span>
                <span className="tag-chip">{stop.timeslot || 'Flexible slot'}</span>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <h4>No route data yet</h4>
          <p>Add activities with locations in the scheduling module to unlock route optimization.</p>
        </div>
      )}
    </ModuleShell>
  );
}

export function ActivitySchedulingModule() {
  const { plans, currentUser, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [days, setDays] = useState([]);
  const [locations, setLocations] = useState([]);
  const [feedback, setFeedback] = useState(defaultFeedback);
  const [editingActivity, setEditingActivity] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    dayId: '',
    locationId: '',
    timeslot: 'MORNING',
    startTime: '09:00',
    endTime: '10:30',
    duration: 90,
    status: 'PLANNED',
  });

  const selectedPlan = useMemo(
    () => plans.find((plan) => String(plan.id) === String(selectedPlanId)),
    [plans, selectedPlanId]
  );

  const loadSchedulingData = async () => {
    if (!selectedPlan) {
      setDays([]);
      setLocations([]);
      return;
    }

    const [dayData, locationData] = await Promise.all([
      getTravelPlanDays(selectedPlan.id),
      getLocationsByDestination(selectedPlan.destinationId),
    ]);

    setDays(dayData);
    setLocations(locationData);
    setFormData((current) => ({
      ...current,
      dayId: current.dayId || String(dayData[0]?.id || ''),
      locationId: current.locationId || String(locationData[0]?.id || ''),
    }));
  };

  useEffect(() => {
    loadSchedulingData().catch((loadError) => {
      console.error(loadError);
      setFeedback({
        type: 'error',
        message: 'Scheduling data could not be loaded for this plan.',
      });
    });
  }, [selectedPlanId]);

  const allActivities = useMemo(() => flattenActivities(days), [days]);

  const overlapWarnings = useMemo(() => {
    return allActivities.filter(
      (activity) =>
        activity.dayId === Number(formData.dayId) &&
        activity.timeslot === formData.timeslot &&
        activity.id !== editingActivity?.id
    );
  }, [allActivities, formData.dayId, formData.timeslot, editingActivity]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const payload = {
      name: formData.name,
      description: formData.description,
      dayId: Number(formData.dayId),
      createdBy: currentUser?.id ? Number(currentUser.id) : null,
      locationId: Number(formData.locationId),
      timeslot: formData.timeslot,
      startTime: formData.startTime,
      endTime: formData.endTime,
      duration: Number(formData.duration),
      status: formData.status,
    };

    try {
      if (editingActivity) {
        await updateActivity(editingActivity.id, payload);
      } else {
        await createActivity(payload);
      }

      setEditingActivity(null);
      setFormData({
        name: '',
        description: '',
        dayId: String(days[0]?.id || ''),
        locationId: String(locations[0]?.id || ''),
        timeslot: 'MORNING',
        startTime: '09:00',
        endTime: '10:30',
        duration: 90,
        status: 'PLANNED',
      });
      setFeedback({
        type: 'success',
        message: 'Activity saved and assigned to the selected time slot.',
      });
      await loadSchedulingData();
    } catch (saveError) {
      console.error(saveError);
      setFeedback({
        type: 'error',
        message: saveError.response?.data?.message || 'The activity could not be saved.',
      });
    }
  };

  const handleEdit = (activity) => {
    setEditingActivity(activity);
    setFormData({
      name: activity.name,
      description: activity.description || '',
      dayId: String(activity.dayId),
      locationId: String(activity.locationId),
      timeslot: activity.timeslot || 'MORNING',
      startTime: activity.startTime || '09:00',
      endTime: activity.endTime || '10:30',
      duration: activity.duration || 90,
      status: activity.status || 'PLANNED',
    });
  };

  const handleDelete = async (activityId) => {
    try {
      await deleteActivity(activityId);
      await loadSchedulingData();
    } catch (deleteError) {
      console.error(deleteError);
      setFeedback({
        type: 'error',
        message: 'Activity deletion failed.',
      });
    }
  };

  return (
    <ModuleShell
      title="Activity Scheduling"
      description="Assign activities into morning, midday, or evening slots and keep the daily plan readable."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Scheduling notes</p>
          <h3>Conflict awareness</h3>
          <p className="body-copy">
            The module warns when you place multiple activities in the same day and slot, helping avoid overloaded itineraries.
          </p>
        </>
      }
    >
      <div className="stack-list">
        {feedback.message ? (
          <div className={`feedback-panel ${feedback.type === 'success' ? 'feedback-success' : 'feedback-error'}`}>
            <strong>{feedback.type === 'success' ? 'Saved' : 'Action needed'}</strong>
            <p>{feedback.message}</p>
          </div>
        ) : null}

        <form onSubmit={handleSubmit} className="planning-form">
          <div className="form-group full-span">
            <label>Activity name</label>
            <input value={formData.name} onChange={(event) => setFormData({ ...formData, name: event.target.value })} required />
          </div>
          <div className="form-group">
            <label>Day</label>
            <select value={formData.dayId} onChange={(event) => setFormData({ ...formData, dayId: event.target.value })} required>
              {days.map((day) => (
                <option key={day.id} value={day.id}>
                  {formatDate(day.date)}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Location</label>
            <select value={formData.locationId} onChange={(event) => setFormData({ ...formData, locationId: event.target.value })} required>
              {locations.map((location) => (
                <option key={location.id} value={location.id}>
                  {location.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Timeslot</label>
            <select value={formData.timeslot} onChange={(event) => setFormData({ ...formData, timeslot: event.target.value })}>
              <option value="MORNING">Morning</option>
              <option value="MIDDAY">Midday</option>
              <option value="EVENING">Evening</option>
            </select>
          </div>
          <div className="form-group">
            <label>Status</label>
            <select value={formData.status} onChange={(event) => setFormData({ ...formData, status: event.target.value })}>
              <option value="PLANNED">PLANNED</option>
              <option value="CONFIRMED">CONFIRMED</option>
              <option value="OPTIONAL">OPTIONAL</option>
            </select>
          </div>
          <div className="form-group">
            <label>Start time</label>
            <input type="time" value={formData.startTime} onChange={(event) => setFormData({ ...formData, startTime: event.target.value })} />
          </div>
          <div className="form-group">
            <label>End time</label>
            <input type="time" value={formData.endTime} onChange={(event) => setFormData({ ...formData, endTime: event.target.value })} />
          </div>
          <div className="form-group">
            <label>Duration (minutes)</label>
            <input type="number" min="15" step="15" value={formData.duration} onChange={(event) => setFormData({ ...formData, duration: event.target.value })} />
          </div>
          <div className="form-group full-span">
            <label>Description</label>
            <textarea value={formData.description} onChange={(event) => setFormData({ ...formData, description: event.target.value })} rows="3" />
          </div>
          <div className="form-actions full-span">
            <button type="submit" className="primary-button">
              {editingActivity ? 'Update activity' : 'Add activity'}
            </button>
          </div>
        </form>

        {overlapWarnings.length ? (
          <div className="feedback-panel feedback-warning">
            <strong>Potential overlap</strong>
            <p>
              {overlapWarnings.length} other activit{overlapWarnings.length === 1 ? 'y is' : 'ies are'} already in this slot.
            </p>
          </div>
        ) : null}

        {days.map((day) => (
          <section key={day.id} className="module-subsection">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Day plan</p>
                <h3>{formatDate(day.date)}</h3>
              </div>
            </div>
            {day.activities?.length ? (
              <div className="stack-list">
                {day.activities.map((activity) => (
                  <article key={activity.id} className="trip-row">
                    <div>
                      <strong>{activity.name}</strong>
                      <p>
                        {activity.locationName} · {activity.timeslot || 'Flexible'} · {activity.startTime || '--'} - {activity.endTime || '--'}
                      </p>
                    </div>
                    <div className="module-actions-inline">
                      <button type="button" className="ghost-button slim-button" onClick={() => handleEdit(activity)}>
                        Edit
                      </button>
                      <button type="button" className="ghost-button slim-button" onClick={() => handleDelete(activity.id)}>
                        Delete
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <div className="empty-state compact-empty">
                <h4>No activities yet</h4>
                <p>Use the form above to populate this day.</p>
              </div>
            )}
          </section>
        ))}
      </div>
    </ModuleShell>
  );
}

export function AttractionRecommendationsModule() {
  const { plans, currentUser, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [preferences, setPreferences] = useState([]);
  const [categories, setCategories] = useState([]);
  const [interest, setInterest] = useState('culture');
  const [recommendations, setRecommendations] = useState([]);

  useEffect(() => {
    if (!currentUser) {
      return;
    }

    Promise.all([getUserPreferences(currentUser.id), getCategories()])
      .then(([preferenceData, categoryData]) => {
        setPreferences(preferenceData);
        setCategories(categoryData);
      })
      .catch((loadError) => console.error(loadError));
  }, [currentUser]);

  useEffect(() => {
    if (!selectedPlanId) {
      setRecommendations([]);
      return;
    }

    getAttractions(selectedPlanId, interest)
      .then(setRecommendations)
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId, interest]);

  const savePreference = async () => {
    if (!currentUser) {
      return;
    }

    await createUserPreference(currentUser.id, {
      preferenceType: 'interest',
      preferenceValue: interest,
    });

    const updatedPreferences = await getUserPreferences(currentUser.id);
    setPreferences(updatedPreferences);
  };

  return (
    <ModuleShell
      title="Attraction Recommendations"
      description="Recommendations are tied to the selected destination and refined by saved user interests."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Preference memory</p>
          <h3>Saved interests</h3>
          <div className="tag-row">
            {preferences.length ? preferences.map((preference) => (
              <span key={preference.id} className="tag-chip">
                {preference.preferenceValue}
              </span>
            )) : <span className="tag-chip">No saved preferences yet</span>}
          </div>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <select value={interest} onChange={(event) => setInterest(event.target.value)}>
            <option value="culture">Culture</option>
            <option value="nature">Nature</option>
            <option value="food">Food</option>
            <option value="fun">Fun</option>
            {categories.map((category) => (
              <option key={category.id} value={category.name.toLowerCase()}>
                {category.name}
              </option>
            ))}
          </select>
          <button type="button" className="ghost-button" onClick={savePreference}>
            Save as preference
          </button>
        </div>

        {recommendations.length ? (
          recommendations.map((item) => (
            <article key={item.locationId} className="trip-row">
              <div>
                <strong>{item.name}</strong>
                <p>
                  {item.type} · {item.address}
                </p>
                <p>{item.reason}</p>
              </div>
              <div className="trip-row-meta">
                <span className="status-pill status-live">{item.matchScore}% match</span>
              </div>
            </article>
          ))
        ) : (
          <div className="empty-state">
            <h4>No recommendations available</h4>
            <p>Create locations for this destination or try another interest category.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function WeatherForecastModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [forecast, setForecast] = useState([]);

  useEffect(() => {
    if (!selectedPlanId) {
      setForecast([]);
      return;
    }

    getWeatherForecast(selectedPlanId)
      .then(setForecast)
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  return (
    <ModuleShell
      title="Weather Forecast"
      description="Forecasts are generated per day of the selected travel plan and include activity guidance."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Usage</p>
          <h3>Planning hint</h3>
          <p className="body-copy">Use rainy days for museums and dining, then move outdoor stops to sunnier slots.</p>
        </>
      }
    >
      <div className="feature-grid single-column-grid">
        {forecast.map((day) => (
          <article key={day.date} className="feature-card">
            <div className="feature-card-header">
              <span className="nav-symbol large">{day.condition.slice(0, 2).toUpperCase()}</span>
              <span className="status-pill status-live">{day.temperatureCelsius}°C</span>
            </div>
            <h4>{formatDate(day.date)}</h4>
            <p>{day.condition}</p>
            <p>{day.recommendation}</p>
            <div className="tag-row">
              <span className="tag-chip">{day.suggestedTimeslot}</span>
            </div>
          </article>
        ))}
      </div>
    </ModuleShell>
  );
}

export function BudgetManagementModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [budgets, setBudgets] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [feedback, setFeedback] = useState(defaultFeedback);
  const [budgetForm, setBudgetForm] = useState({ totalAmount: '', currency: 'EUR' });
  const [expenseForm, setExpenseForm] = useState({ amount: '', category: 'Transport', date: '' });

  const selectedPlan = useMemo(
    () => plans.find((plan) => String(plan.id) === String(selectedPlanId)),
    [plans, selectedPlanId]
  );

  const tripDays = useMemo(() => {
    if (!selectedPlan?.startDate || !selectedPlan?.endDate) {
      return 0;
    }

    const difference = Math.round((new Date(selectedPlan.endDate) - new Date(selectedPlan.startDate)) / (1000 * 60 * 60 * 24)) + 1;
    return difference;
  }, [selectedPlan]);

  const estimatedBudget = useMemo(() => {
    if (!selectedPlan) {
      return 0;
    }

    const destination = (selectedPlan.destinationName || '').toLowerCase();
    let dailyAverage = 110;
    if (destination.includes('paris') || destination.includes('tokyo')) dailyAverage = 170;
    if (destination.includes('sarajevo') || destination.includes('zagreb')) dailyAverage = 95;
    return tripDays * dailyAverage;
  }, [selectedPlan, tripDays]);

  const loadFinanceData = async () => {
    if (!selectedPlanId) {
      setBudgets([]);
      setExpenses([]);
      return;
    }

    const [budgetData, expenseData] = await Promise.all([
      getBudgetsByPlan(Number(selectedPlanId)),
      getExpensesByPlan(Number(selectedPlanId)),
    ]);

    setBudgets(budgetData);
    setExpenses(expenseData);
    if (budgetData[0]) {
      setBudgetForm({
        totalAmount: budgetData[0].totalAmount,
        currency: budgetData[0].currency || 'EUR',
      });
    } else {
      setBudgetForm({ totalAmount: estimatedBudget, currency: 'EUR' });
    }
  };

  useEffect(() => {
    loadFinanceData().catch((loadError) => {
      console.error(loadError);
      setFeedback({
        type: 'error',
        message: 'Budget data could not be loaded.',
      });
    });
  }, [selectedPlanId, estimatedBudget]);

  const totalBudget = Number(budgets[0]?.totalAmount || 0);
  const totalSpent = expenses.reduce((sum, expense) => sum + Number(expense.amount || 0), 0);
  const remaining = totalBudget - totalSpent;

  const saveBudget = async () => {
    const payload = {
      totalAmount: Number(budgetForm.totalAmount),
      currency: budgetForm.currency,
      planId: Number(selectedPlanId),
    };

    if (budgets[0]) {
      await updateBudget(budgets[0].id, payload);
    } else {
      await createBudget(payload);
    }

    await loadFinanceData();
    setFeedback({
      type: 'success',
      message: 'Budget saved successfully.',
    });
  };

  const addExpense = async () => {
    await createExpense({
      amount: Number(expenseForm.amount),
      category: expenseForm.category,
      date: expenseForm.date || null,
      planId: Number(selectedPlanId),
    });

    setExpenseForm({ amount: '', category: 'Transport', date: '' });
    await loadFinanceData();
  };

  return (
    <ModuleShell
      title="Budget Management"
      description="Track planned budget, real expenses, and destination-based cost estimates for the selected trip."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Estimator</p>
          <h3>Destination average</h3>
          <p className="body-copy">
            Estimated budget based on destination profile and trip length: <strong>{estimatedBudget} EUR</strong>.
          </p>
        </>
      }
    >
      <div className="stack-list">
        {feedback.message ? (
          <div className={`feedback-panel ${feedback.type === 'success' ? 'feedback-success' : 'feedback-error'}`}>
            <strong>{feedback.type === 'success' ? 'Updated' : 'Action needed'}</strong>
            <p>{feedback.message}</p>
          </div>
        ) : null}

        <div className="mini-stat-grid">
          <div className="mini-stat">
            <strong>{totalBudget || estimatedBudget}</strong>
            <span>budget</span>
          </div>
          <div className="mini-stat">
            <strong>{totalSpent}</strong>
            <span>spent</span>
          </div>
          <div className="mini-stat">
            <strong>{remaining}</strong>
            <span>remaining</span>
          </div>
        </div>

        <div className="module-subsection">
          <div className="module-toolbar">
            <input
              type="number"
              value={budgetForm.totalAmount}
              onChange={(event) => setBudgetForm({ ...budgetForm, totalAmount: event.target.value })}
              placeholder="Budget amount"
            />
            <select value={budgetForm.currency} onChange={(event) => setBudgetForm({ ...budgetForm, currency: event.target.value })}>
              <option value="EUR">EUR</option>
              <option value="USD">USD</option>
              <option value="BAM">BAM</option>
            </select>
            <button type="button" className="primary-button" onClick={saveBudget}>
              Save budget
            </button>
          </div>
        </div>

        <div className="module-subsection">
          <div className="module-toolbar">
            <input
              type="number"
              value={expenseForm.amount}
              onChange={(event) => setExpenseForm({ ...expenseForm, amount: event.target.value })}
              placeholder="Expense amount"
            />
            <select value={expenseForm.category} onChange={(event) => setExpenseForm({ ...expenseForm, category: event.target.value })}>
              <option value="Transport">Transport</option>
              <option value="Accommodation">Accommodation</option>
              <option value="Food">Food</option>
              <option value="Tickets">Tickets</option>
            </select>
            <input type="datetime-local" value={expenseForm.date} onChange={(event) => setExpenseForm({ ...expenseForm, date: event.target.value })} />
            <button type="button" className="ghost-button" onClick={addExpense}>
              Add expense
            </button>
          </div>
        </div>

        {expenses.length ? expenses.map((expense) => (
          <article key={expense.id} className="trip-row">
            <div>
              <strong>{expense.amount}</strong>
              <p>{expense.category}</p>
            </div>
            <div className="module-actions-inline">
              <span>{formatDateTime(expense.date)}</span>
              <button type="button" className="ghost-button slim-button" onClick={() => deleteExpense(expense.id).then(loadFinanceData)}>
                Delete
              </button>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No expenses logged</h4>
            <p>Add real costs as they appear during planning or travel.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function CollaborativePlanningModule() {
  const { plans, currentUser, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [users, setUsers] = useState([]);
  const [memberships, setMemberships] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [role, setRole] = useState('EDITOR');

  const loadCollaborationData = async () => {
    const userList = await getAllUsers();
    setUsers(userList);

    const membershipGroups = await Promise.all(
      userList.map(async (user) => ({
        user,
        memberships: await getPlanMembershipsForUser(user.id),
      }))
    );

    const planMemberships = membershipGroups
      .flatMap((entry) =>
        entry.memberships
          .filter((membership) => String(membership.planId) === String(selectedPlanId))
          .map((membership) => ({ ...membership, user: entry.user }))
      );

    setMemberships(planMemberships);
    if (!selectedUserId && userList.length > 0) {
      setSelectedUserId(String(userList[0].id));
    }
  };

  useEffect(() => {
    if (!selectedPlanId) {
      setMemberships([]);
      return;
    }

    loadCollaborationData().catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  const addMember = async () => {
    await createPlanMembership(Number(selectedUserId), {
      planId: Number(selectedPlanId),
      role,
    });

    await loadCollaborationData();
  };

  return (
    <ModuleShell
      title="Collaborative Planning"
      description="Attach collaborators to a plan, coordinate roles, and prepare the plan for shared decision-making."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Shared editing</p>
          <h3>Collaboration model</h3>
          <p className="body-copy">
            Membership data lives in the user service, while activities and votes stay connected to the plan itself.
          </p>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <select value={selectedUserId} onChange={(event) => setSelectedUserId(event.target.value)}>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.username} ({user.email})
              </option>
            ))}
          </select>
          <select value={role} onChange={(event) => setRole(event.target.value)}>
            <option value="EDITOR">EDITOR</option>
            <option value="VIEWER">VIEWER</option>
            <option value="COORDINATOR">COORDINATOR</option>
          </select>
          <button type="button" className="primary-button" onClick={addMember}>
            Add collaborator
          </button>
        </div>

        {memberships.length ? memberships.map((membership) => (
          <article key={membership.id} className="trip-row">
            <div>
              <strong>{membership.user?.username || `User ${membership.userId}`}</strong>
              <p>{membership.user?.email || 'No email available'}</p>
            </div>
            <div className="module-actions-inline">
              <span className="tag-chip">{membership.role}</span>
              {membership.userId !== currentUser?.id ? (
                <button
                  type="button"
                  className="ghost-button slim-button"
                  onClick={() => deletePlanMembership(membership.id).then(loadCollaborationData)}
                >
                  Remove
                </button>
              ) : null}
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No collaborators yet</h4>
            <p>Add people to this plan so they can coordinate activities and decisions.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function ActivityVotingModule() {
  const { plans, currentUser, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [days, setDays] = useState([]);
  const [selectedActivityId, setSelectedActivityId] = useState('');
  const [votes, setVotes] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewForm, setReviewForm] = useState({ rating: 5, comment: '' });

  const loadActivities = async () => {
    if (!selectedPlanId) {
      setDays([]);
      return;
    }

    const dayData = await getTravelPlanDays(selectedPlanId);
    setDays(dayData);
    const firstActivity = flattenActivities(dayData)[0];
    if (firstActivity && !selectedActivityId) {
      setSelectedActivityId(String(firstActivity.id));
    }
  };

  const activities = useMemo(() => flattenActivities(days), [days]);

  const loadVotesAndReviews = async () => {
    if (!selectedActivityId) {
      setVotes([]);
      setReviews([]);
      return;
    }

    const [voteData, reviewData] = await Promise.all([
      getVotesByActivity(Number(selectedActivityId)),
      getReviewsByActivity(Number(selectedActivityId)),
    ]);
    setVotes(voteData);
    setReviews(reviewData);
  };

  useEffect(() => {
    loadActivities().catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  useEffect(() => {
    loadVotesAndReviews().catch((loadError) => console.error(loadError));
  }, [selectedActivityId]);

  const currentVote = votes.find((vote) => Number(vote.userId) === Number(currentUser?.id));

  const submitVote = async () => {
    if (currentVote || !currentUser) {
      return;
    }

    await createVote({
      userId: Number(currentUser.id),
      activityId: Number(selectedActivityId),
    });
    await loadVotesAndReviews();
  };

  const submitReview = async () => {
    if (!currentUser) {
      return;
    }

    await createReview({
      userId: Number(currentUser.id),
      activityId: Number(selectedActivityId),
      rating: Number(reviewForm.rating),
      comment: reviewForm.comment,
    });
    setReviewForm({ rating: 5, comment: '' });
    await loadVotesAndReviews();
  };

  return (
    <ModuleShell
      title="Activity Voting"
      description="Group members can vote for activities and leave lightweight reviews to support shared decisions."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Decision signal</p>
          <h3>Live summary</h3>
          <p className="body-copy">Votes come from the communication service and are tied to real activity IDs.</p>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <select value={selectedActivityId} onChange={(event) => setSelectedActivityId(event.target.value)}>
            <option value="">Choose an activity...</option>
            {activities.map((activity) => (
              <option key={activity.id} value={activity.id}>
                {activity.name} · {formatDate(activity.dayDate)}
              </option>
            ))}
          </select>

          {currentVote ? (
            <button type="button" className="ghost-button" onClick={() => deleteVote(currentVote.id).then(loadVotesAndReviews)}>
              Remove my vote
            </button>
          ) : (
            <button type="button" className="primary-button" onClick={submitVote} disabled={!selectedActivityId}>
              Vote for activity
            </button>
          )}
        </div>

        <div className="mini-stat-grid">
          <div className="mini-stat">
            <strong>{votes.length}</strong>
            <span>votes</span>
          </div>
          <div className="mini-stat">
            <strong>{reviews.length}</strong>
            <span>reviews</span>
          </div>
          <div className="mini-stat">
            <strong>{reviews.length ? (reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length).toFixed(1) : '0.0'}</strong>
            <span>avg rating</span>
          </div>
        </div>

        <div className="module-toolbar">
          <select value={reviewForm.rating} onChange={(event) => setReviewForm({ ...reviewForm, rating: event.target.value })}>
            <option value="5">5 stars</option>
            <option value="4">4 stars</option>
            <option value="3">3 stars</option>
            <option value="2">2 stars</option>
            <option value="1">1 star</option>
          </select>
          <input
            value={reviewForm.comment}
            onChange={(event) => setReviewForm({ ...reviewForm, comment: event.target.value })}
            placeholder="Add a quick comment"
          />
          <button type="button" className="ghost-button" onClick={submitReview}>
            Submit review
          </button>
        </div>

        {reviews.length ? reviews.map((review) => (
          <article key={review.id} className="trip-row">
            <div>
              <strong>User {review.userId}</strong>
              <p>{review.comment}</p>
            </div>
            <div className="trip-row-meta">
              <span className="tag-chip">{review.rating}/5</span>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No reviews yet</h4>
            <p>Use votes and comments to help the group align on final inclusions.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function ReservationManagementModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [reservations, setReservations] = useState([]);
  const [premiumReservations, setPremiumReservations] = useState([]);
  const [formData, setFormData] = useState({
    type: 'HOTEL',
    details: '',
    startDate: '',
    endDate: '',
    price: '',
    status: 'PENDING',
  });

  const loadReservations = async () => {
    if (!selectedPlanId) {
      setReservations([]);
      setPremiumReservations([]);
      return;
    }

    const [reservationData, premiumData] = await Promise.all([
      getReservationsForPlan(Number(selectedPlanId)),
      getPremiumReservations(Number(selectedPlanId), 200),
    ]);

    setReservations(reservationData);
    setPremiumReservations(premiumData);
  };

  useEffect(() => {
    loadReservations().catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  const submitReservation = async () => {
    await createReservation({
      ...formData,
      startDate: formData.startDate,
      endDate: formData.endDate,
      price: Number(formData.price),
      planId: Number(selectedPlanId),
    });

    setFormData({
      type: 'HOTEL',
      details: '',
      startDate: '',
      endDate: '',
      price: '',
      status: 'PENDING',
    });
    await loadReservations();
  };

  return (
    <ModuleShell
      title="Reservation Management"
      description="Track hotel, flight, and activity reservations linked directly to a real travel plan."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Premium filter</p>
          <h3>High-value bookings</h3>
          <p className="body-copy">{premiumReservations.length} reservations currently exceed the premium threshold.</p>
        </>
      }
    >
      <div className="stack-list">
        <div className="planning-form">
          <div className="form-group">
            <label>Type</label>
            <select value={formData.type} onChange={(event) => setFormData({ ...formData, type: event.target.value })}>
              <option value="HOTEL">HOTEL</option>
              <option value="FLIGHT">FLIGHT</option>
              <option value="ACTIVITY">ACTIVITY</option>
            </select>
          </div>
          <div className="form-group">
            <label>Status</label>
            <select value={formData.status} onChange={(event) => setFormData({ ...formData, status: event.target.value })}>
              <option value="PENDING">PENDING</option>
              <option value="CONFIRMED">CONFIRMED</option>
            </select>
          </div>
          <div className="form-group full-span">
            <label>Details</label>
            <input value={formData.details} onChange={(event) => setFormData({ ...formData, details: event.target.value })} />
          </div>
          <div className="form-group">
            <label>Start</label>
            <input type="datetime-local" value={formData.startDate} onChange={(event) => setFormData({ ...formData, startDate: event.target.value })} />
          </div>
          <div className="form-group">
            <label>End</label>
            <input type="datetime-local" value={formData.endDate} onChange={(event) => setFormData({ ...formData, endDate: event.target.value })} />
          </div>
          <div className="form-group">
            <label>Price</label>
            <input type="number" value={formData.price} onChange={(event) => setFormData({ ...formData, price: event.target.value })} />
          </div>
          <div className="form-actions full-span">
            <button type="button" className="primary-button" onClick={submitReservation}>
              Save reservation
            </button>
          </div>
        </div>

        {reservations.length ? reservations.map((reservation) => (
          <article key={reservation.id} className="trip-row">
            <div>
              <strong>{reservation.type}</strong>
              <p>{reservation.details}</p>
              <p>{formatDateTime(reservation.startDate)} to {formatDateTime(reservation.endDate)}</p>
            </div>
            <div className="module-actions-inline">
              <span className="tag-chip">{reservation.status}</span>
              <span>{reservation.price}</span>
              <button type="button" className="ghost-button slim-button" onClick={() => deleteReservation(reservation.id).then(loadReservations)}>
                Delete
              </button>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No reservations yet</h4>
            <p>Create reservations for hotels, flights, or activities and keep them attached to the plan.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function OfflineAccessModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [days, setDays] = useState([]);
  const [forecast, setForecast] = useState([]);

  useEffect(() => {
    if (!selectedPlanId) {
      setDays([]);
      setForecast([]);
      return;
    }

    Promise.all([getTravelPlanDays(selectedPlanId), getWeatherForecast(selectedPlanId)])
      .then(([dayData, forecastData]) => {
        setDays(dayData);
        setForecast(forecastData);
      })
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  const downloadOfflinePack = () => {
    const blob = new Blob([JSON.stringify({ days, forecast }, null, 2)], { type: 'application/json' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `travel-plan-${selectedPlanId}-offline-pack.json`;
    link.click();
    URL.revokeObjectURL(link.href);
  };

  const openPrintableVersion = () => {
    const printWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printWindow) {
      return;
    }

    const html = `
      <html>
        <head><title>Travel Plan ${selectedPlanId}</title></head>
        <body style="font-family: Arial, sans-serif; padding: 24px;">
          <h1>Travel Plan ${selectedPlanId}</h1>
          ${days
            .map(
              (day) => `
                <section style="margin-bottom: 24px;">
                  <h2>${formatDate(day.date)}</h2>
                  <ul>
                    ${(day.activities || [])
                      .map((activity) => `<li>${activity.name} - ${activity.locationName} (${activity.timeslot || 'Flexible'})</li>`)
                      .join('')}
                  </ul>
                </section>
              `
            )
            .join('')}
        </body>
      </html>
    `;

    printWindow.document.write(html);
    printWindow.document.close();
    printWindow.focus();
  };

  return (
    <ModuleShell
      title="Offline Access"
      description="Prepare a downloadable offline pack and printable summary so the trip remains usable without connectivity."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Export options</p>
          <h3>Offline package</h3>
          <p className="body-copy">Export JSON for device storage and open a print-friendly view you can save as PDF.</p>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <button type="button" className="primary-button" onClick={downloadOfflinePack}>
            Download offline pack
          </button>
          <button type="button" className="ghost-button" onClick={openPrintableVersion}>
            Open print view
          </button>
        </div>

        <div className="mini-stat-grid">
          <div className="mini-stat">
            <strong>{days.length}</strong>
            <span>days bundled</span>
          </div>
          <div className="mini-stat">
            <strong>{flattenActivities(days).length}</strong>
            <span>activities bundled</span>
          </div>
          <div className="mini-stat">
            <strong>{forecast.length}</strong>
            <span>forecast entries</span>
          </div>
        </div>
      </div>
    </ModuleShell>
  );
}

export function NotificationsModule() {
  const { plans, currentUser, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [notifications, setNotifications] = useState([]);
  const [formData, setFormData] = useState({
    message: '',
    date: '',
    type: 'REMINDER',
  });

  const loadNotifications = async () => {
    if (!selectedPlanId) {
      setNotifications([]);
      return;
    }

    const data = await getNotificationsByPlan(Number(selectedPlanId));
    setNotifications(data);
  };

  useEffect(() => {
    loadNotifications().catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  const submitNotification = async () => {
    if (!currentUser) {
      return;
    }

    await createNotification({
      message: formData.message,
      date: formData.date,
      userId: Number(currentUser.id),
      planId: Number(selectedPlanId),
      type: formData.type,
    });

    setFormData({ message: '', date: '', type: 'REMINDER' });
    await loadNotifications();
  };

  return (
    <ModuleShell
      title="Notifications and Reminders"
      description="Create reminders for departures, activities, and reservation deadlines from the communication service."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Reminder design</p>
          <h3>Common use cases</h3>
          <ul className="feature-list compact-list">
            <li>Airport departure reminders</li>
            <li>Reservation check-in prompts</li>
            <li>Daily activity wake-up alerts</li>
          </ul>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <input
            value={formData.message}
            onChange={(event) => setFormData({ ...formData, message: event.target.value })}
            placeholder="Reminder message"
          />
          <input
            type="datetime-local"
            value={formData.date}
            onChange={(event) => setFormData({ ...formData, date: event.target.value })}
          />
          <select value={formData.type} onChange={(event) => setFormData({ ...formData, type: event.target.value })}>
            <option value="REMINDER">REMINDER</option>
            <option value="TRANSPORT">TRANSPORT</option>
            <option value="RESERVATION">RESERVATION</option>
          </select>
          <button type="button" className="primary-button" onClick={submitNotification}>
            Create notification
          </button>
        </div>

        {notifications.length ? notifications.map((notification) => (
          <article key={notification.id} className="trip-row">
            <div>
              <strong>{notification.message}</strong>
              <p>{notification.type}</p>
            </div>
            <div className="module-actions-inline">
              <span>{formatDateTime(notification.date)}</span>
              <button type="button" className="ghost-button slim-button" onClick={() => deleteNotification(notification.id).then(loadNotifications)}>
                Delete
              </button>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No reminders yet</h4>
            <p>Add reminders so important trip events are not missed.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function ScheduleLoadModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [scheduleLoad, setScheduleLoad] = useState(null);

  useEffect(() => {
    if (!selectedPlanId) {
      setScheduleLoad(null);
      return;
    }

    getScheduleLoad(selectedPlanId)
      .then(setScheduleLoad)
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  return (
    <ModuleShell
      title="Schedule Load Evaluation"
      description="The backend analyzes daily activity counts and durations to flag overloaded days before the trip."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Summary</p>
          <h3>Trip pacing</h3>
          <p className="body-copy">{scheduleLoad?.summary || 'Select a plan to evaluate its pacing.'}</p>
        </>
      }
    >
      <div className="stack-list">
        {scheduleLoad?.dailyLoads?.length ? scheduleLoad.dailyLoads.map((day) => (
          <article key={day.dayId} className="trip-row">
            <div>
              <strong>{formatDate(day.date)}</strong>
              <p>{day.activityCount} activities · {day.totalDurationMinutes} minutes</p>
            </div>
            <div className="trip-row-meta">
              <span className={`status-pill ${day.intensity === 'High' ? 'status-prototype' : 'status-live'}`}>{day.intensity}</span>
              <span>{day.warning}</span>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No load analysis yet</h4>
            <p>Add activities to this plan so the load evaluator has real data to score.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function LocalRecommendationsModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [recommendations, setRecommendations] = useState([]);

  useEffect(() => {
    if (!selectedPlanId) {
      setRecommendations([]);
      return;
    }

    getLocalRecommendations(selectedPlanId)
      .then(setRecommendations)
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  return (
    <ModuleShell
      title="Local Recommendations"
      description="Surface nearby restaurants, events, and local context linked to the selected trip destination."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Context</p>
          <h3>Best usage</h3>
          <p className="body-copy">Use these as flexible additions when the main itinerary leaves breathing room.</p>
        </>
      }
    >
      <div className="feature-grid single-column-grid">
        {recommendations.map((item) => (
          <article key={item.locationId} className="feature-card">
            <div className="feature-card-header">
              <span className="nav-symbol large">{item.type.slice(0, 2).toUpperCase()}</span>
              <span className="tag-chip">{item.bestTimeslot}</span>
            </div>
            <h4>{item.name}</h4>
            <p>{item.address}</p>
            <p>{item.context}</p>
          </article>
        ))}
      </div>
    </ModuleShell>
  );
}

export function TravelPlanSharingModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [sharedLinks, setSharedLinks] = useState([]);
  const [days, setDays] = useState([]);
  const [shareType, setShareType] = useState('PUBLIC_VIEW');

  const loadSharingData = async () => {
    if (!selectedPlanId) {
      setSharedLinks([]);
      return;
    }

    const [links, dayData] = await Promise.all([
      getSharedLinksByPlan(Number(selectedPlanId)),
      getTravelPlanDays(Number(selectedPlanId)),
    ]);
    setSharedLinks(links);
    setDays(dayData);
  };

  useEffect(() => {
    loadSharingData().catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  const generateLink = async () => {
    const shareUrl = `${window.location.origin}/plans/${selectedPlanId}?mode=${shareType.toLowerCase()}`;
    await createSharedLink({
      url: shareUrl,
      planId: Number(selectedPlanId),
      type: shareType,
    });
    await loadSharingData();
  };

  const openPrintableShare = () => {
    const printableWindow = window.open('', '_blank', 'width=900,height=700');
    if (!printableWindow) {
      return;
    }

    printableWindow.document.write(`
      <html>
        <head><title>Shared Travel Plan</title></head>
        <body style="font-family: Arial, sans-serif; padding: 24px;">
          <h1>Shared Travel Plan</h1>
          ${days
            .map(
              (day) => `
                <section style="margin-bottom: 24px;">
                  <h2>${formatDate(day.date)}</h2>
                  <ul>
                    ${(day.activities || [])
                      .map((activity) => `<li>${activity.name} - ${activity.locationName}</li>`)
                      .join('')}
                  </ul>
                </section>
              `
            )
            .join('')}
        </body>
      </html>
    `);
    printableWindow.document.close();
  };

  return (
    <ModuleShell
      title="Travel Plan Sharing"
      description="Generate sharable links for plan review and open a clean printable version for handoff or PDF export."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Sharing formats</p>
          <h3>Review modes</h3>
          <p className="body-copy">Use public-view links for quick sharing and print view when a PDF-style handoff is needed.</p>
        </>
      }
    >
      <div className="stack-list">
        <div className="module-toolbar">
          <select value={shareType} onChange={(event) => setShareType(event.target.value)}>
            <option value="PUBLIC_VIEW">PUBLIC_VIEW</option>
            <option value="PDF_READY">PDF_READY</option>
          </select>
          <button type="button" className="primary-button" onClick={generateLink}>
            Generate share link
          </button>
          <button type="button" className="ghost-button" onClick={openPrintableShare}>
            Open printable view
          </button>
        </div>

        {sharedLinks.length ? sharedLinks.map((link) => (
          <article key={link.id} className="trip-row">
            <div>
              <strong>{link.type}</strong>
              <p>{link.url}</p>
            </div>
            <div className="module-actions-inline">
              <a className="ghost-button slim-button" href={link.url} target="_blank" rel="noreferrer">
                Open
              </a>
              <button type="button" className="ghost-button slim-button" onClick={() => deleteSharedLink(link.id).then(loadSharingData)}>
                Delete
              </button>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No share links yet</h4>
            <p>Create a public or PDF-ready link for stakeholders and travel companions.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}

export function WaitingTimeModule() {
  const { plans, loading, error } = useWorkspaceBootstrap();
  const [selectedPlanId, setSelectedPlanId] = useSelectedPlan(plans);
  const [insights, setInsights] = useState([]);

  useEffect(() => {
    if (!selectedPlanId) {
      setInsights([]);
      return;
    }

    getWaitingTimeInsights(selectedPlanId)
      .then(setInsights)
      .catch((loadError) => console.error(loadError));
  }, [selectedPlanId]);

  return (
    <ModuleShell
      title="Waiting Time Analysis"
      description="Estimate queue pressure and best visit windows for scheduled attractions using the backend waiting-time model."
      loading={loading}
      error={error}
      plans={plans}
      selectedPlanId={selectedPlanId}
      setSelectedPlanId={setSelectedPlanId}
      aside={
        <>
          <p className="eyebrow">Interpretation</p>
          <h3>What to watch</h3>
          <p className="body-copy">Activities over 30 minutes expected wait usually deserve an earlier slot or a backup plan.</p>
        </>
      }
    >
      <div className="stack-list">
        {insights.length ? insights.map((item) => (
          <article key={item.activityId} className="trip-row">
            <div>
              <strong>{item.activityName}</strong>
              <p>{item.locationName}</p>
              <p>{item.advice}</p>
            </div>
            <div className="trip-row-meta">
              <span>{item.expectedWaitMinutes} min</span>
              <span className="tag-chip">{item.suggestedWindow}</span>
            </div>
          </article>
        )) : (
          <div className="empty-state compact-empty">
            <h4>No waiting-time insights yet</h4>
            <p>Schedule attractions first so the analysis engine has real activity data to assess.</p>
          </div>
        )}
      </div>
    </ModuleShell>
  );
}
