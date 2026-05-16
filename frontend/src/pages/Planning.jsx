import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  getTravelPlans,
  createTravelPlan,
  updateTravelPlan,
  deleteTravelPlan,
  getDestinations,
  createDestination,
  getDays,
  createDay,
  getActivities,
  addActivityToDay,
  updateActivity,
  deleteActivity,
  getLocations,
  createLocation,
  getPlanReservations,
  requestSagaReservation,
} from '../api/planningService';
import {
  getBudgetsByPlanId,
  createBudget,
  deleteBudget,
  getExpensesByPlanId,
  createExpense,
  deleteExpense,
  getPagedReservations,
  getPremiumReservations,
  createReservation,
  deleteReservation,
  getSagaReservations,
} from '../api/financeService';
import {
  getNotificationsByUserId,
  createNotification,
  deleteNotification,
  getVotesByActivityId,
  createVote,
  deleteVote,
  getSharedLinksByPlanId,
  createSharedLink,
  deleteSharedLink,
} from '../api/communicationService';
import {
  getAllUsers,
  createPreference,
  createPlanMembership,
  getUserPlanMemberships,
} from '../api/userService';
import { getFinancePlanId } from '../utils/financePlanMap';
import {
  buildAttractionRecommendations,
  buildNearbyRecommendations,
  buildWeatherActivityRecommendations,
  buildWaitTimeInsights,
  calculateWorkload,
  calculateRouteMetrics,
  downloadPlanSnapshot,
  enumerateTripDates,
  estimateTripBudget,
  estimateWaitTime,
  exportPlanSnapshot,
  findScheduleConflict,
  loadForecast,
  loadOfflineSnapshot,
  optimizeRoute,
  saveOfflineSnapshot,
} from '../utils/tripInsights';

const defaultPlanForm = {
  name: '',
  startDate: '',
  endDate: '',
  destinationId: '',
  description: '',
  status: 'PLANNED',
};

const defaultDestinationForm = {
  name: '',
};

const defaultLocationForm = {
  name: '',
  destinationId: '',
  address: '',
  latitude: '',
  longitude: '',
  type: 'Attraction',
};

const defaultActivityForm = {
  name: '',
  description: '',
  dayId: '',
  locationId: '',
  timeslot: 'MORNING',
  startTime: '09:00',
  endTime: '10:00',
  duration: 60,
  status: 'PLANNED',
};

const defaultBudgetForm = {
  totalAmount: '',
  currency: 'EUR',
};

const defaultExpenseForm = {
  amount: '',
  category: 'Transport',
  date: new Date().toISOString().slice(0, 16),
};

const defaultReservationForm = {
  type: 'HOTEL',
  details: '',
  startDate: '',
  endDate: '',
  price: '',
  status: 'CONFIRMED',
};

const defaultSagaRequestForm = {
  userId: '',
  reservationType: 'HOTEL',
  itemName: '',
  startDate: '',
  endDate: '',
  amount: '',
  currency: 'EUR',
  simulateFinanceFailure: false,
  simulatePlanningFinalizationFailure: false,
};

const defaultPreferenceForm = {
  preferenceType: 'Interest',
  preferenceValue: '',
};

const defaultMembershipForm = {
  userId: '',
  role: 'EDITOR',
};

const defaultNotificationForm = {
  message: '',
  date: new Date().toISOString().slice(0, 16),
  type: 'REMINDER',
};

const defaultSharedLinkForm = {
  url: '',
  type: 'PUBLIC',
};

const fallbackDestinationNames = [
  'Rome, Italy',
  'Paris, France',
  'Barcelona, Spain',
  'Vienna, Austria',
  'Prague, Czech Republic',
  'Budapest, Hungary',
  'Amsterdam, Netherlands',
  'Berlin, Germany',
  'Lisbon, Portugal',
  'Athens, Greece',
  'Copenhagen, Denmark',
  'Stockholm, Sweden',
  'Dublin, Ireland',
  'Brussels, Belgium',
  'Zurich, Switzerland',
  'Florence, Italy',
  'Venice, Italy',
  'Munich, Germany',
  'Seville, Spain',
  'Krakow, Poland',
];

const fallbackDestinationOptions = fallbackDestinationNames.map((name, index) => ({
  id: 1000 + index,
  name,
}));

function toLocalDateTime(value) {
  return value ? new Date(value).toISOString().slice(0, 19) : null;
}

function DestinationPicker({ value, onChange, options, placeholder = 'Choose city or country' }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');

  const selectedDestination = useMemo(
    () => options.find((destination) => String(destination.id) === String(value)) || null,
    [options, value],
  );

  const filteredOptions = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    if (!normalizedQuery) {
      return options;
    }

    return options.filter((destination) => destination.name.toLowerCase().includes(normalizedQuery));
  }, [options, query]);

  useEffect(() => {
    if (!open) {
      setQuery('');
    }
  }, [open]);

  return (
    <div className="picker-shell">
      <button
        aria-expanded={open}
        className={`picker-trigger ${open ? 'is-open' : ''}`}
        onClick={() => setOpen((previous) => !previous)}
        type="button"
      >
        <span>{selectedDestination?.name || placeholder}</span>
        <span className="picker-caret">{open ? '▲' : '▼'}</span>
      </button>
      {open && (
        <div className="picker-panel">
          <div className="picker-title">Available destinations</div>
          <input
            autoFocus
            className="picker-search"
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Search city or country"
            type="text"
            value={query}
          />
          <div className="picker-options">
            {filteredOptions.map((destination) => (
              <button
                className={`picker-option ${String(destination.id) === String(value) ? 'is-selected' : ''}`}
                key={destination.id}
                onClick={() => {
                  onChange(String(destination.id));
                  setOpen(false);
                }}
                type="button"
              >
                {destination.name}
              </button>
            ))}
            {filteredOptions.length === 0 && (
              <div className="picker-empty">No destinations match your search.</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function WorkspaceSection({ title, description, meta, defaultOpen = true, children }) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <section className={`workspace-section ${open ? 'is-open' : ''}`}>
      <button
        aria-expanded={open}
        className="workspace-section-header"
        onClick={() => setOpen((previous) => !previous)}
        type="button"
      >
        <div className="workspace-section-copy">
          <h3>{title}</h3>
          {description ? <p>{description}</p> : null}
        </div>
        <div className="workspace-section-actions">
          {meta ? <span className="workspace-section-meta">{meta}</span> : null}
          <span className="workspace-section-toggle">{open ? 'Hide' : 'Show'}</span>
        </div>
      </button>
      {open ? <div className="workspace-section-body">{children}</div> : null}
    </section>
  );
}

const workspaceTabs = [
  {
    id: 'overview',
    title: 'Overview',
    description: 'Forecast, recommendations, and trip health',
  },
  {
    id: 'itinerary',
    title: 'Itinerary',
    description: 'Days, activities, route order, and wait-time guidance',
  },
  {
    id: 'finance',
    title: 'Finance',
    description: 'Budgets, expenses, reservations, and saga flow',
  },
  {
    id: 'collaboration',
    title: 'Collaboration',
    description: 'Members, voting, and shared planning',
  },
  {
    id: 'notifications',
    title: 'Notifications',
    description: 'Reminders, feed, and upcoming alerts',
  },
];

export default function Planning() {
  const { currentUser, refreshProfile } = useAuth();
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [activeTab, setActiveTab] = useState('overview');
  const [feedback, setFeedback] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const [plans, setPlans] = useState([]);
  const [destinations, setDestinations] = useState([]);
  const [days, setDays] = useState([]);
  const [activities, setActivities] = useState([]);
  const [locations, setLocations] = useState([]);
  const [users, setUsers] = useState([]);

  const [selectedPlanId, setSelectedPlanId] = useState(null);

  const [forecast, setForecast] = useState(null);
  const [offlineSnapshot, setOfflineSnapshot] = useState(null);

  const [budgets, setBudgets] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [premiumReservations, setPremiumReservations] = useState([]);
  const [sagaReservations, setSagaReservations] = useState([]);
  const [planReservations, setPlanReservations] = useState([]);
  const [memberships, setMemberships] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [sharedLinks, setSharedLinks] = useState([]);
  const [votesByActivity, setVotesByActivity] = useState({});

  const [planForm, setPlanForm] = useState(defaultPlanForm);
  const [destinationForm, setDestinationForm] = useState(defaultDestinationForm);
  const [locationForm, setLocationForm] = useState(defaultLocationForm);
  const [activityForm, setActivityForm] = useState(defaultActivityForm);
  const [budgetForm, setBudgetForm] = useState(defaultBudgetForm);
  const [expenseForm, setExpenseForm] = useState(defaultExpenseForm);
  const [reservationForm, setReservationForm] = useState(defaultReservationForm);
  const [sagaRequestForm, setSagaRequestForm] = useState(defaultSagaRequestForm);
  const [preferenceForm, setPreferenceForm] = useState(defaultPreferenceForm);
  const [membershipForm, setMembershipForm] = useState(defaultMembershipForm);
  const [notificationForm, setNotificationForm] = useState(defaultNotificationForm);
  const [sharedLinkForm, setSharedLinkForm] = useState(defaultSharedLinkForm);

  useEffect(() => {
    if (currentUser) {
      setSagaRequestForm((prev) => ({ ...prev, userId: currentUser.id }));
    }
  }, [currentUser]);

  useEffect(() => {
    const loadInitialData = async () => {
      setLoading(true);
      setErrorMessage('');

      try {
        const [planData, destinationData, dayData, activityData, locationData, userData] =
          await Promise.all([
            getTravelPlans(),
            getDestinations(),
            getDays(),
            getActivities(),
            getLocations(),
            getAllUsers().catch(() => []),
          ]);

        setPlans(planData);
        setDestinations(destinationData);
        setDays(dayData);
        setActivities(activityData);
        setLocations(locationData);
        setUsers(userData);

        if (planData.length > 0) {
          setSelectedPlanId(planData[0].id);
        }

        if (destinationData.length > 0) {
          const preferredDestination =
            destinationData
              .filter((destination) => !/^rabbitmq\b/i.test(destination.name || ''))
              .sort((left, right) => left.name.localeCompare(right.name))[0] || destinationData[0];

          setPlanForm((prev) => ({ ...prev, destinationId: String(preferredDestination.id) }));
          setLocationForm((prev) => ({ ...prev, destinationId: String(preferredDestination.id) }));
        }
      } catch (error) {
        setErrorMessage(error.response?.data?.message || 'The workspace could not load its data.');
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, []);

  const availableDestinations = useMemo(() => {
    const filteredDestinations = destinations
      .filter((destination) => !/^rabbitmq\b/i.test(destination.name || ''))
      .sort((left, right) => left.name.localeCompare(right.name));

    return filteredDestinations.length > 0
      ? filteredDestinations
      : destinations.length > 0
        ? [...destinations].sort((left, right) => left.name.localeCompare(right.name))
        : fallbackDestinationOptions;
  }, [destinations]);

  const selectedPlan = useMemo(
    () => plans.find((plan) => plan.id === selectedPlanId) || null,
    [plans, selectedPlanId],
  );

  const otherPlans = useMemo(
    () => plans.filter((plan) => plan.id !== selectedPlanId),
    [plans, selectedPlanId],
  );

  const selectedPlanDays = useMemo(
    () =>
      days
        .filter((day) => day.travelPlanId === selectedPlanId)
        .sort((left, right) => left.date.localeCompare(right.date)),
    [days, selectedPlanId],
  );

  const activitiesByDay = useMemo(() => {
    const map = new Map();
    selectedPlanDays.forEach((day) => {
      map.set(
        day.id,
        activities
          .filter((activity) => activity.dayId === day.id)
          .sort((left, right) => `${left.startTime || ''}`.localeCompare(`${right.startTime || ''}`)),
      );
    });
    return map;
  }, [activities, selectedPlanDays]);

  const currentPlanActivities = useMemo(
    () => selectedPlanDays.flatMap((day) => activitiesByDay.get(day.id) || []),
    [activitiesByDay, selectedPlanDays],
  );

  const currentWorkload = useMemo(() => calculateWorkload(currentPlanActivities), [currentPlanActivities]);

  const optimizedRoutesByDay = useMemo(() => {
    const routes = {};

    selectedPlanDays.forEach((day) => {
      routes[day.id] = optimizeRoute(activitiesByDay.get(day.id) || [], locations);
    });

    return routes;
  }, [activitiesByDay, locations, selectedPlanDays]);

  const routeMetricsByDay = useMemo(() => {
    const metrics = {};

    selectedPlanDays.forEach((day) => {
      metrics[day.id] = calculateRouteMetrics(optimizedRoutesByDay[day.id] || [], locations);
    });

    return metrics;
  }, [locations, optimizedRoutesByDay, selectedPlanDays]);

  const attractionRecommendations = useMemo(() => {
    if (!selectedPlan || !currentUser) {
      return [];
    }

    return buildAttractionRecommendations(
      locations,
      selectedPlan.destinationId,
      currentUser.preferences || [],
    );
  }, [currentUser, locations, selectedPlan]);

  const nearbySuggestions = useMemo(() => {
    if (!selectedPlan) {
      return [];
    }

    return buildNearbyRecommendations(locations, selectedPlan.destinationId);
  }, [locations, selectedPlan]);

  const weatherActivityRecommendations = useMemo(
    () => buildWeatherActivityRecommendations(forecast, attractionRecommendations, nearbySuggestions),
    [attractionRecommendations, forecast, nearbySuggestions],
  );

  const waitTimeInsights = useMemo(
    () => buildWaitTimeInsights(currentPlanActivities),
    [currentPlanActivities],
  );

  const budgetProjection = useMemo(
    () => estimateTripBudget(selectedPlan, selectedPlanDays, currentPlanActivities, reservations),
    [currentPlanActivities, reservations, selectedPlan, selectedPlanDays],
  );

  const budgetCoverage = useMemo(() => {
    const savedBudget = budgets.reduce((sum, budget) => sum + Number(budget.totalAmount || 0), 0);
    return {
      savedBudget,
      difference: Math.round((savedBudget - budgetProjection.estimatedTotal) * 100) / 100,
    };
  }, [budgetProjection.estimatedTotal, budgets]);

  const collaborationMetrics = useMemo(() => {
    const contributors = currentPlanActivities.reduce((map, activity) => {
      const currentCount = map.get(activity.createdBy) || 0;
      map.set(activity.createdBy, currentCount + 1);
      return map;
    }, new Map());
    const voteTotal = Object.values(votesByActivity).reduce((sum, voteList) => sum + voteList.length, 0);

    return {
      contributorRows: Array.from(contributors.entries())
        .map(([userId, count]) => ({
          userId,
          username: users.find((user) => user.id === userId)?.username || `User ${userId}`,
          count,
        }))
        .sort((left, right) => right.count - left.count),
      voteTotal,
    };
  }, [currentPlanActivities, users, votesByActivity]);

  const activityConflictPreview = useMemo(() => {
    if (!activityForm.dayId) {
      return null;
    }

    return findScheduleConflict(
      activitiesByDay.get(Number(activityForm.dayId)) || [],
      {
        dayId: Number(activityForm.dayId),
        startTime: activityForm.startTime,
        endTime: activityForm.endTime,
        timeslot: activityForm.timeslot,
      },
    );
  }, [activitiesByDay, activityForm.dayId, activityForm.endTime, activityForm.startTime, activityForm.timeslot]);

  const upcomingNotifications = useMemo(
    () =>
      [...notifications]
        .sort((left, right) => `${left.date}`.localeCompare(`${right.date}`))
        .slice(0, 5),
    [notifications],
  );

  const workspaceHighlights = useMemo(
    () => [
      {
        label: 'Trip days',
        value: `${selectedPlanDays.length}`,
        detail: selectedPlanDays.length > 0 ? 'Auto-generated and editable' : 'Created after plan setup',
      },
      {
        label: 'Scheduled activities',
        value: `${currentPlanActivities.length}`,
        detail: `${currentWorkload.level} workload`,
      },
      {
        label: 'Collaborators',
        value: `${memberships.length}`,
        detail: `${collaborationMetrics.voteTotal} votes collected`,
      },
      {
        label: 'Budget forecast',
        value: `€${budgetProjection.estimatedTotal}`,
        detail: `${upcomingNotifications.length} upcoming reminders`,
      },
    ],
    [
      budgetProjection.estimatedTotal,
      collaborationMetrics.voteTotal,
      currentPlanActivities.length,
      currentWorkload.level,
      memberships.length,
      selectedPlanDays.length,
      upcomingNotifications.length,
    ],
  );

  useEffect(() => {
    const loadPlanScopedData = async () => {
      if (!selectedPlan || !currentUser) {
        return;
      }

      setRefreshing(true);
      setErrorMessage('');

      try {
        const financePlanId = getFinancePlanId(selectedPlan.id);
        const [
          forecastData,
          budgetData,
          expenseData,
          reservationData,
          premiumData,
          sagaData,
          planReservationData,
          notificationData,
          sharedLinkData,
          membershipData,
        ] = await Promise.all([
          loadForecast(selectedPlan, locations),
          getBudgetsByPlanId(financePlanId),
          getExpensesByPlanId(financePlanId),
          getPagedReservations(financePlanId),
          getPremiumReservations(financePlanId, 150),
          getSagaReservations(selectedPlan.id),
          getPlanReservations(selectedPlan.id),
          getNotificationsByUserId(currentUser.id),
          getSharedLinksByPlanId(selectedPlan.id),
          loadMembershipsForPlan(selectedPlan.id, users),
        ]);

        setForecast(forecastData);
        setBudgets(budgetData);
        setExpenses(expenseData);
        setReservations(reservationData);
        setPremiumReservations(premiumData);
        setSagaReservations(sagaData);
        setPlanReservations(planReservationData);
        setNotifications(notificationData.filter((item) => item.planId === selectedPlan.id));
        setSharedLinks(sharedLinkData);
        setMemberships(membershipData);
        setOfflineSnapshot(loadOfflineSnapshot(selectedPlan.id));

        const voteEntries = await Promise.all(
          currentPlanActivities.map(async (activity) => [
            activity.id,
            await getVotesByActivityId(activity.id),
          ]),
        );
        setVotesByActivity(Object.fromEntries(voteEntries));
      } catch (error) {
        setErrorMessage(error.response?.data?.message || 'Plan data could not be refreshed.');
      } finally {
        setRefreshing(false);
      }
    };

    loadPlanScopedData();
  }, [currentPlanActivities, currentUser, locations, selectedPlan, users]);

  const itinerarySnapshot = useMemo(() => {
    if (!selectedPlan) {
      return null;
    }

    return {
      plan: selectedPlan,
      workload: currentWorkload,
      days: selectedPlanDays.map((day) => ({
        ...day,
        activities: activitiesByDay.get(day.id) || [],
      })),
    };
  }, [activitiesByDay, currentWorkload, selectedPlan, selectedPlanDays]);

  async function loadMembershipsForPlan(planId, availableUsers) {
    const lists = await Promise.all(
      availableUsers.map(async (user) => {
        try {
          return await getUserPlanMemberships(user.id);
        } catch {
          return [];
        }
      }),
    );

    return lists.flat().filter((membership) => membership.planId === Number(planId));
  }

  async function refreshPlanningData() {
    const [planData, dayData, activityData, locationData, destinationData] = await Promise.all([
      getTravelPlans(),
      getDays(),
      getActivities(),
      getLocations(),
      getDestinations(),
    ]);

    setPlans(planData);
    setDays(dayData);
    setActivities(activityData);
    setLocations(locationData);
    setDestinations(destinationData);
  }

  async function handleCreatePlan(event) {
    event.preventDefault();
    setFeedback('');
    setErrorMessage('');

    if (planForm.endDate < planForm.startDate) {
      setErrorMessage('The end date must be the same as or later than the start date.');
      return;
    }

    try {
      let destinationId = Number(planForm.destinationId);
      const selectedFallbackDestination = fallbackDestinationOptions.find(
        (destination) => Number(destination.id) === destinationId,
      );

      if (selectedFallbackDestination && !destinations.some((destination) => Number(destination.id) === destinationId)) {
        const createdDestination = await createDestination({ name: selectedFallbackDestination.name });
        destinationId = Number(createdDestination.id);
        const refreshedDestinations = await getDestinations();
        setDestinations(refreshedDestinations);
      }

      const createdPlan = await createTravelPlan({
        ...planForm,
        destinationId,
      });

      const generatedDates = enumerateTripDates(planForm.startDate, planForm.endDate);
      await Promise.all(
        generatedDates.map((date) =>
          createDay({
            date,
            travelPlanId: createdPlan.id,
          }),
        ),
      );

      await refreshPlanningData();
      setSelectedPlanId(createdPlan.id);
      setPlanForm({
        ...defaultPlanForm,
        destinationId: availableDestinations[0] ? String(availableDestinations[0].id) : '',
      });
      setFeedback('Travel plan created and its day structure was generated automatically.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Travel plan creation failed.');
    }
  }

  async function handlePlanUpdate() {
    if (!selectedPlan) {
      return;
    }

    try {
      await updateTravelPlan(selectedPlan.id, {
        name: selectedPlan.name,
        startDate: selectedPlan.startDate,
        endDate: selectedPlan.endDate,
        destinationId: selectedPlan.destinationId,
        description: selectedPlan.description,
        status: selectedPlan.status,
      });
      await refreshPlanningData();
      setFeedback('The selected travel plan was refreshed.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Travel plan update failed.');
    }
  }

  async function handleDeletePlan(planId) {
    try {
      await deleteTravelPlan(planId);
      await refreshPlanningData();
      setSelectedPlanId((previous) => (previous === planId ? null : previous));
      setFeedback('The travel plan was removed.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Travel plan deletion failed.');
    }
  }

  async function handleCreateDestination(event) {
    event.preventDefault();

    try {
      await createDestination(destinationForm);
      const refreshedDestinations = await getDestinations();
      setDestinations(refreshedDestinations);
      setDestinationForm(defaultDestinationForm);
      const preferredDestination =
        refreshedDestinations
          .filter((destination) => !/^rabbitmq\b/i.test(destination.name || ''))
          .sort((left, right) => left.name.localeCompare(right.name))[0] || refreshedDestinations[0];
      setPlanForm((prev) => ({ ...prev, destinationId: preferredDestination ? String(preferredDestination.id) : '' }));
      setFeedback('Destination added to the planning catalog.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Destination creation failed.');
    }
  }

  async function handleCreateLocation(event) {
    event.preventDefault();

    try {
      let destinationId = Number(locationForm.destinationId);
      const selectedFallbackDestination = fallbackDestinationOptions.find(
        (destination) => Number(destination.id) === destinationId,
      );

      if (selectedFallbackDestination && !destinations.some((destination) => Number(destination.id) === destinationId)) {
        const createdDestination = await createDestination({ name: selectedFallbackDestination.name });
        destinationId = Number(createdDestination.id);
        const refreshedDestinations = await getDestinations();
        setDestinations(refreshedDestinations);
      }

      await createLocation({
        ...locationForm,
        destinationId,
        latitude: locationForm.latitude ? Number(locationForm.latitude) : null,
        longitude: locationForm.longitude ? Number(locationForm.longitude) : null,
      });
      const refreshedLocations = await getLocations();
      setLocations(refreshedLocations);
      setLocationForm({
        ...defaultLocationForm,
        destinationId: selectedPlan?.destinationId ? String(selectedPlan.destinationId) : availableDestinations[0] ? String(availableDestinations[0].id) : '',
      });
      setFeedback('Location saved and available for itinerary planning.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Location creation failed.');
    }
  }

  async function handleCreateActivity(event) {
    event.preventDefault();

    const dayActivities = activitiesByDay.get(Number(activityForm.dayId)) || [];
    const conflict = findScheduleConflict(dayActivities, {
      dayId: Number(activityForm.dayId),
      startTime: activityForm.startTime,
      endTime: activityForm.endTime,
      timeslot: activityForm.timeslot,
    });

    if (activityForm.endTime <= activityForm.startTime) {
      setErrorMessage('The activity end time must be later than the start time.');
      return;
    }

    if (conflict) {
      setErrorMessage(
        `This activity overlaps with ${conflict.name}. Move it to another slot or time window.`,
      );
      return;
    }

    try {
      await addActivityToDay(Number(activityForm.dayId), {
        ...activityForm,
        dayId: Number(activityForm.dayId),
        locationId: Number(activityForm.locationId),
        createdBy: currentUser.id,
        duration: Number(activityForm.duration),
      });
      const refreshedActivities = await getActivities();
      setActivities(refreshedActivities);
      setActivityForm((prev) => ({
        ...defaultActivityForm,
        dayId: prev.dayId,
      }));
      setFeedback('Activity added to the itinerary.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Activity creation failed.');
    }
  }

  async function handleActivityStatusChange(activity, status) {
    try {
      await updateActivity(activity.id, {
        name: activity.name,
        description: activity.description,
        dayId: activity.dayId,
        createdBy: activity.createdBy,
        locationId: activity.locationId,
        timeslot: activity.timeslot,
        startTime: activity.startTime,
        endTime: activity.endTime,
        duration: activity.duration,
        status,
      });
      const refreshedActivities = await getActivities();
      setActivities(refreshedActivities);
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Activity update failed.');
    }
  }

  async function handleDeleteActivity(activityId) {
    try {
      await deleteActivity(activityId);
      const refreshedActivities = await getActivities();
      setActivities(refreshedActivities);
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Activity deletion failed.');
    }
  }

  async function handleSavePreference(event) {
    event.preventDefault();

    try {
      await createPreference(currentUser.id, preferenceForm);
      await refreshProfile();
      setPreferenceForm(defaultPreferenceForm);
      setFeedback('Preference added and ready to power recommendations.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Preference update failed.');
    }
  }

  async function handleCreateBudget(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createBudget({
        totalAmount: Number(budgetForm.totalAmount),
        planId: getFinancePlanId(selectedPlan.id),
        currency: budgetForm.currency,
      });
      setBudgets(await getBudgetsByPlanId(getFinancePlanId(selectedPlan.id)));
      setBudgetForm(defaultBudgetForm);
      setFeedback('Budget created for the selected trip.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Budget creation failed.');
    }
  }

  async function handleDeleteBudget(id) {
    if (!selectedPlan) {
      return;
    }

    try {
      await deleteBudget(id);
      setBudgets(await getBudgetsByPlanId(getFinancePlanId(selectedPlan.id)));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Budget deletion failed.');
    }
  }

  async function handleCreateExpense(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createExpense({
        amount: Number(expenseForm.amount),
        category: expenseForm.category,
        date: toLocalDateTime(expenseForm.date),
        planId: getFinancePlanId(selectedPlan.id),
      });
      setExpenses(await getExpensesByPlanId(getFinancePlanId(selectedPlan.id)));
      setExpenseForm(defaultExpenseForm);
      setFeedback('Expense logged for the selected trip.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Expense creation failed.');
    }
  }

  async function handleDeleteExpense(id) {
    if (!selectedPlan) {
      return;
    }

    try {
      await deleteExpense(id);
      setExpenses(await getExpensesByPlanId(getFinancePlanId(selectedPlan.id)));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Expense deletion failed.');
    }
  }

  async function handleCreateReservation(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createReservation({
        type: reservationForm.type,
        details: reservationForm.details,
        startDate: toLocalDateTime(reservationForm.startDate),
        endDate: toLocalDateTime(reservationForm.endDate),
        price: Number(reservationForm.price),
        status: reservationForm.status,
        planId: getFinancePlanId(selectedPlan.id),
      });
      setReservations(await getPagedReservations(getFinancePlanId(selectedPlan.id)));
      setPremiumReservations(await getPremiumReservations(getFinancePlanId(selectedPlan.id), 150));
      setReservationForm(defaultReservationForm);
      setFeedback('Reservation linked to the selected trip.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Reservation creation failed.');
    }
  }

  async function handleDeleteReservation(id) {
    if (!selectedPlan) {
      return;
    }

    try {
      await deleteReservation(id);
      setReservations(await getPagedReservations(getFinancePlanId(selectedPlan.id)));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Reservation deletion failed.');
    }
  }

  async function handleStartSagaReservation(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await requestSagaReservation(selectedPlan.id, {
        ...sagaRequestForm,
        userId: Number(sagaRequestForm.userId),
        amount: Number(sagaRequestForm.amount),
      });
      setPlanReservations(await getPlanReservations(selectedPlan.id));
      setSagaReservations(await getSagaReservations(selectedPlan.id));
      setFeedback('The asynchronous reservation saga was started successfully.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Saga reservation request failed.');
    }
  }

  async function handleCreateMembership(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createPlanMembership(Number(membershipForm.userId), {
        planId: Number(selectedPlan.id),
        role: membershipForm.role,
      });
      setMemberships(await loadMembershipsForPlan(selectedPlan.id, users));
      setMembershipForm(defaultMembershipForm);
      setFeedback('Plan member added to the collaboration panel.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Plan membership creation failed.');
    }
  }

  async function handleCreateVote(activityId) {
    try {
      await createVote({
        userId: currentUser.id,
        activityId: Number(activityId),
      });
      const refreshedVotes = await getVotesByActivityId(activityId);
      setVotesByActivity((previous) => ({
        ...previous,
        [activityId]: refreshedVotes,
      }));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Voting failed.');
    }
  }

  async function handleDeleteVote(voteId, activityId) {
    try {
      await deleteVote(voteId);
      const refreshedVotes = await getVotesByActivityId(activityId);
      setVotesByActivity((previous) => ({
        ...previous,
        [activityId]: refreshedVotes,
      }));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Vote removal failed.');
    }
  }

  async function handleCreateNotification(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createNotification({
        message: notificationForm.message,
        date: toLocalDateTime(notificationForm.date),
        userId: currentUser.id,
        planId: selectedPlan.id,
        type: notificationForm.type,
      });
      setNotifications(
        (await getNotificationsByUserId(currentUser.id)).filter(
          (notification) => notification.planId === selectedPlan.id,
        ),
      );
      setNotificationForm(defaultNotificationForm);
      setFeedback('Notification stored for the active travel plan.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Notification creation failed.');
    }
  }

  async function handleDeleteNotification(id) {
    try {
      await deleteNotification(id);
      setNotifications(
        (await getNotificationsByUserId(currentUser.id)).filter(
          (notification) => notification.planId === selectedPlan.id,
        ),
      );
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Notification deletion failed.');
    }
  }

  async function handleCreateSharedLink(event) {
    event.preventDefault();

    if (!selectedPlan) {
      return;
    }

    try {
      await createSharedLink({
        url: sharedLinkForm.url,
        planId: selectedPlan.id,
        type: sharedLinkForm.type,
      });
      setSharedLinks(await getSharedLinksByPlanId(selectedPlan.id));
      setSharedLinkForm(defaultSharedLinkForm);
      setFeedback('Shareable plan link created.');
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Shared link creation failed.');
    }
  }

  async function handleDeleteSharedLink(id) {
    if (!selectedPlan) {
      return;
    }

    try {
      await deleteSharedLink(id);
      setSharedLinks(await getSharedLinksByPlanId(selectedPlan.id));
    } catch (error) {
      setErrorMessage(error.response?.data?.message || 'Shared link deletion failed.');
    }
  }

  function handleSaveOffline() {
    if (!selectedPlan || !itinerarySnapshot) {
      return;
    }

    saveOfflineSnapshot(selectedPlan.id, itinerarySnapshot);
    setOfflineSnapshot(loadOfflineSnapshot(selectedPlan.id));
    setFeedback('Offline snapshot stored in the browser for this plan.');
  }

  function handleExportPdf() {
    if (!selectedPlan || !itinerarySnapshot) {
      return;
    }

    exportPlanSnapshot(selectedPlan, itinerarySnapshot);
  }

  function handleDownloadOfflinePlan() {
    if (!selectedPlan || !itinerarySnapshot) {
      return;
    }

    downloadPlanSnapshot(selectedPlan, itinerarySnapshot);
    setFeedback('Offline JSON package downloaded for this plan.');
  }

  function handleUseEstimatedBudget() {
    setBudgetForm((previous) => ({
      ...previous,
      totalAmount: `${budgetProjection.estimatedTotal}`,
    }));
    setActiveTab('finance');
    setFeedback('The estimated trip budget was copied into the budget form.');
  }

  if (loading) {
    return <div className="state-panel">Loading your travel workspace...</div>;
  }

  return (
    <div className="workspace-layout">
      <aside className="workspace-sidebar">
        <div className="panel-card trip-dashboard-card">
          <span className="eyebrow">Trip dashboard</span>
          {selectedPlan ? (
            <>
              <h1>Current trip</h1>
              <button
                className="plan-card is-selected"
                onClick={() => setSelectedPlanId(selectedPlan.id)}
                type="button"
              >
                <strong>{selectedPlan.name}</strong>
                <span>{selectedPlan.destinationName}</span>
                <small>
                  {selectedPlan.startDate} → {selectedPlan.endDate}
                </small>
              </button>
            </>
          ) : (
            <>
              <h1>Choose a trip</h1>
              <p>Please select a plan so we can show additional information.</p>
            </>
          )}

          {otherPlans.length > 0 && (
            <div className="trip-dashboard-group">
              <h2>Previous trips</h2>
              <div className="plan-list">
                {otherPlans.map((plan) => (
                  <button
                    className="plan-card"
                    key={plan.id}
                    onClick={() => setSelectedPlanId(plan.id)}
                    type="button"
                  >
                    <strong>{plan.name}</strong>
                    <span>{plan.destinationName}</span>
                    <small>
                      {plan.startDate} → {plan.endDate}
                    </small>
                  </button>
                ))}
              </div>
            </div>
          )}

          {!selectedPlan && plans.length > 0 && (
            <div className="trip-dashboard-group">
              <h2>Available trips</h2>
              <div className="plan-list">
                {plans.map((plan) => (
                  <button
                    className="plan-card"
                    key={plan.id}
                    onClick={() => setSelectedPlanId(plan.id)}
                    type="button"
                  >
                    <strong>{plan.name}</strong>
                    <span>{plan.destinationName}</span>
                    <small>
                      {plan.startDate} → {plan.endDate}
                    </small>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="panel-card workspace-nav-card">
          <span className="eyebrow">Trip menu</span>
          <h2>Sections</h2>
          <p>
            {selectedPlan
              ? 'Everything for the selected trip is organized here.'
              : 'Please select a plan so we can show additional information.'}
          </p>
          {selectedPlan ? (
            <div className="workspace-nav-list">
              {workspaceTabs.map((tab) => (
                <button
                  className={`workspace-nav-button ${activeTab === tab.id ? 'is-active' : ''}`}
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  type="button"
                >
                  <div className="workspace-nav-copy">
                    <strong>{tab.title}</strong>
                    <small>{tab.description}</small>
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="subtle-note">Select a trip from the dashboard to unlock the trip menu.</div>
          )}
        </div>
        <form className="panel-card form-stack" onSubmit={handleCreatePlan}>
          <h2>Start a new trip</h2>
          <label>
            Plan name
            <input
              required
              value={planForm.name}
              onChange={(event) => setPlanForm({ ...planForm, name: event.target.value })}
            />
          </label>
          <label>
            Destination
            <DestinationPicker
              onChange={(destinationId) => setPlanForm({ ...planForm, destinationId })}
              options={availableDestinations}
              value={planForm.destinationId}
            />
          </label>
          <div className="split-fields">
            <label>
              Start date
              <input
                required
                type="date"
                value={planForm.startDate}
                onChange={(event) => setPlanForm({ ...planForm, startDate: event.target.value })}
              />
            </label>
            <label>
              End date
              <input
                required
                type="date"
                value={planForm.endDate}
                onChange={(event) => setPlanForm({ ...planForm, endDate: event.target.value })}
              />
            </label>
          </div>
          <label>
            Description
            <textarea
              rows="3"
              value={planForm.description}
              onChange={(event) => setPlanForm({ ...planForm, description: event.target.value })}
            />
          </label>
          <button className="primary-button" type="submit">
            Create plan and days
          </button>
        </form>
      </aside>

      <section className="workspace-main">
        {selectedPlan ? (
          <>
            <div className="headline-card">
              <div>
                <span className="eyebrow">Selected trip</span>
                <h2>{selectedPlan.name}</h2>
                <p>
                  {selectedPlan.destinationName} • {selectedPlan.startDate} → {selectedPlan.endDate}
                </p>
              </div>
              <div className="headline-actions">
                <span className={`status-pill status-${selectedPlan.status?.toLowerCase()}`}>
                  {selectedPlan.status}
                </span>
                <button className="secondary-button" onClick={handlePlanUpdate} type="button">
                  Sync selected plan
                </button>
                <button
                  className="danger-button"
                  onClick={() => handleDeletePlan(selectedPlan.id)}
                  type="button"
                >
                  Delete plan
                </button>
              </div>
            </div>

            {feedback && <div className="inline-success">{feedback}</div>}
            {errorMessage && <div className="inline-error">{errorMessage}</div>}
            {refreshing && <div className="subtle-note">Refreshing plan-specific data...</div>}

            <div className="workspace-highlights">
              {workspaceHighlights.map((item) => (
                <article className="highlight-card" key={item.label}>
                  <span>{item.label}</span>
                  <strong>{item.value}</strong>
                  <small>{item.detail}</small>
                </article>
              ))}
            </div>

            {activeTab === 'overview' && (
              <div className="workspace-stack">
                <div className="workspace-metric-grid">
                  <article className="panel-card">
                    <h3>Weather outlook</h3>
                    {forecast ? (
                      <>
                        <p className="metric-value">{forecast.temperature}</p>
                        <p>{forecast.summary}</p>
                        <small>
                          {forecast.advice} • {forecast.source}
                        </small>
                      </>
                    ) : (
                      <p>No forecast available yet.</p>
                    )}
                  </article>

                  <article className="panel-card">
                    <h3>Schedule load analysis</h3>
                    <p className="metric-value">{currentWorkload.level}</p>
                    <p>Score: {Math.round(currentWorkload.score)}</p>
                    <small>{currentWorkload.message}</small>
                  </article>

                  <article className="panel-card">
                    <h3>Estimated trip cost</h3>
                    <p className="metric-value">€{budgetProjection.estimatedTotal}</p>
                    <p>{budgetProjection.summary}</p>
                    <small>
                      Budget gap: {budgetCoverage.difference >= 0 ? '+' : ''}
                      {budgetCoverage.difference} EUR against saved budgets.
                    </small>
                    <div className="inline-actions">
                      <button className="secondary-button" onClick={handleUseEstimatedBudget} type="button">
                        Use estimate in budget form
                      </button>
                    </div>
                  </article>
                </div>

                <WorkspaceSection
                  defaultOpen
                  description="Attractions, restaurants, events, and timing guidance tuned to the selected destination."
                  meta={`${attractionRecommendations.length + nearbySuggestions.length} suggestions`}
                  title="Smart recommendations"
                >
                  <div className="content-grid">
                    <article className="panel-card panel-span-two">
                      <h3>Attraction recommendations</h3>
                      <div className="chip-grid">
                        {attractionRecommendations.map((location) => (
                          <div className="insight-chip" key={location.id}>
                            <strong>{location.name}</strong>
                            <span>{location.type}</span>
                            <small>{location.reason}</small>
                          </div>
                        ))}
                        {attractionRecommendations.length === 0 && (
                          <p>No attraction suggestions yet. Add locations or preferences first.</p>
                        )}
                      </div>
                    </article>

                    <article className="panel-card panel-span-two">
                      <h3>Restaurants and nearby events</h3>
                      <div className="chip-grid">
                        {nearbySuggestions.map((location) => (
                          <div className="insight-chip" key={location.id}>
                            <strong>{location.name}</strong>
                            <span>{location.type}</span>
                            <small>{location.bestMoment}</small>
                          </div>
                        ))}
                        {nearbySuggestions.length === 0 && (
                          <p>Add restaurants, cafes, or event venues to unlock local suggestions.</p>
                        )}
                      </div>
                    </article>

                    <article className="panel-card panel-span-two">
                      <h3>Weather-based activity guidance</h3>
                      <div className="simple-list">
                        {weatherActivityRecommendations.map((item) => (
                          <div className="simple-row" key={item}>
                            <strong>Recommendation</strong>
                            <span>{item}</span>
                          </div>
                        ))}
                        {weatherActivityRecommendations.length === 0 && (
                          <p>Weather-aware guidance will appear after the forecast loads.</p>
                        )}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen={false}
                  description="Manage preferences, offline packages, and the destination catalog without crowding the trip overview."
                  meta={`${availableDestinations.length} destinations`}
                  title="Trip toolkit"
                >
                  <div className="content-grid">
                    <article className="panel-card">
                      <h3>Offline access</h3>
                      <p>Save the current plan snapshot locally for offline access during the trip.</p>
                      <div className="inline-actions">
                        <button className="secondary-button" onClick={handleSaveOffline} type="button">
                          Save offline snapshot
                        </button>
                        <button className="primary-button" onClick={handleExportPdf} type="button">
                          Download or print PDF
                        </button>
                        <button className="ghost-button" onClick={handleDownloadOfflinePlan} type="button">
                          Download JSON
                        </button>
                      </div>
                      {offlineSnapshot && (
                        <small>
                          Cached itinerary available with {offlineSnapshot.days.length} generated day entries.
                        </small>
                      )}
                    </article>

                    <article className="panel-card">
                      <h3>Preference-driven suggestions</h3>
                      <form className="form-stack" onSubmit={handleSavePreference}>
                        <label>
                          Preference type
                          <select
                            value={preferenceForm.preferenceType}
                            onChange={(event) =>
                              setPreferenceForm({
                                ...preferenceForm,
                                preferenceType: event.target.value,
                              })
                            }
                          >
                            <option>Interest</option>
                            <option>Food</option>
                            <option>Travel style</option>
                          </select>
                        </label>
                        <label>
                          Preference value
                          <input
                            required
                            value={preferenceForm.preferenceValue}
                            onChange={(event) =>
                              setPreferenceForm({
                                ...preferenceForm,
                                preferenceValue: event.target.value,
                              })
                            }
                          />
                        </label>
                        <button className="secondary-button" type="submit">
                          Save preference
                        </button>
                      </form>
                      <div className="simple-list">
                        {(currentUser.preferences || []).map((preference) => (
                          <div className="simple-row" key={preference.id}>
                            <strong>{preference.preferenceType}</strong>
                            <span>{preference.preferenceValue}</span>
                          </div>
                        ))}
                      </div>
                    </article>

                    <article className="panel-card panel-span-two">
                      <h3>Destination management</h3>
                      <form className="form-stack" onSubmit={handleCreateDestination}>
                        <label>
                          New destination
                          <input
                            required
                            value={destinationForm.name}
                            onChange={(event) =>
                              setDestinationForm({ ...destinationForm, name: event.target.value })
                            }
                          />
                        </label>
                        <button className="secondary-button" type="submit">
                          Add destination
                        </button>
                      </form>
                      <div className="simple-list">
                        {availableDestinations.map((destination) => (
                          <div className="simple-row" key={destination.id}>
                            <strong>{destination.name}</strong>
                            <span>ID {destination.id}</span>
                          </div>
                        ))}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>
              </div>
            )}

            {activeTab === 'itinerary' && (
              <div className="workspace-stack">
                <WorkspaceSection
                  defaultOpen
                  description="Create locations and activities in a focused editor before moving to the day-by-day itinerary."
                  meta={`${selectedPlanDays.length} days`}
                  title="Plan builder"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Add location</h3>
                      <form className="form-stack" onSubmit={handleCreateLocation}>
                        <label>
                          Name
                          <input
                            required
                            value={locationForm.name}
                            onChange={(event) =>
                              setLocationForm({ ...locationForm, name: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Destination
                          <DestinationPicker
                            onChange={(destinationId) =>
                              setLocationForm({ ...locationForm, destinationId })
                            }
                            options={availableDestinations}
                            value={locationForm.destinationId}
                          />
                        </label>
                        <label>
                          Address
                          <input
                            required
                            value={locationForm.address}
                            onChange={(event) =>
                              setLocationForm({ ...locationForm, address: event.target.value })
                            }
                          />
                        </label>
                        <div className="split-fields">
                          <label>
                            Latitude
                            <input
                              value={locationForm.latitude}
                              onChange={(event) =>
                                setLocationForm({ ...locationForm, latitude: event.target.value })
                              }
                            />
                          </label>
                          <label>
                            Longitude
                            <input
                              value={locationForm.longitude}
                              onChange={(event) =>
                                setLocationForm({ ...locationForm, longitude: event.target.value })
                              }
                            />
                          </label>
                        </div>
                        <label>
                          Type
                          <input
                            required
                            value={locationForm.type}
                            onChange={(event) =>
                              setLocationForm({ ...locationForm, type: event.target.value })
                            }
                          />
                        </label>
                        <button className="secondary-button" type="submit">
                          Save location
                        </button>
                      </form>
                    </article>

                    <article className="panel-card">
                      <h3>Add activity</h3>
                      <form className="form-stack" onSubmit={handleCreateActivity}>
                        <label>
                          Activity name
                          <input
                            required
                            value={activityForm.name}
                            onChange={(event) =>
                              setActivityForm({ ...activityForm, name: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Day
                          <select
                            required
                            value={activityForm.dayId}
                            onChange={(event) =>
                              setActivityForm({ ...activityForm, dayId: event.target.value })
                            }
                          >
                            <option value="">Select day</option>
                            {selectedPlanDays.map((day) => (
                              <option key={day.id} value={day.id}>
                                {day.date}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label>
                          Location
                          <select
                            required
                            value={activityForm.locationId}
                            onChange={(event) =>
                              setActivityForm({ ...activityForm, locationId: event.target.value })
                            }
                          >
                            <option value="">Select location</option>
                            {locations
                              .filter((location) => location.destinationId === selectedPlan.destinationId)
                              .map((location) => (
                                <option key={location.id} value={location.id}>
                                  {location.name}
                                </option>
                              ))}
                          </select>
                        </label>
                        <label>
                          Description
                          <textarea
                            rows="2"
                            value={activityForm.description}
                            onChange={(event) =>
                              setActivityForm({ ...activityForm, description: event.target.value })
                            }
                          />
                        </label>
                        <div className="split-fields">
                          <label>
                            Timeslot
                            <select
                              value={activityForm.timeslot}
                              onChange={(event) =>
                                setActivityForm({ ...activityForm, timeslot: event.target.value })
                              }
                            >
                              <option value="MORNING">Morning</option>
                              <option value="NOON">Noon</option>
                              <option value="EVENING">Evening</option>
                            </select>
                          </label>
                          <label>
                            Duration in minutes
                            <input
                              min="15"
                              required
                              type="number"
                              value={activityForm.duration}
                              onChange={(event) =>
                                setActivityForm({ ...activityForm, duration: event.target.value })
                              }
                            />
                          </label>
                        </div>
                        <div className="split-fields">
                          <label>
                            Start time
                            <input
                              required
                              type="time"
                              value={activityForm.startTime}
                              onChange={(event) =>
                                setActivityForm({ ...activityForm, startTime: event.target.value })
                              }
                            />
                          </label>
                          <label>
                            End time
                            <input
                              required
                              type="time"
                              value={activityForm.endTime}
                              onChange={(event) =>
                                setActivityForm({ ...activityForm, endTime: event.target.value })
                              }
                            />
                          </label>
                        </div>
                        <button className="primary-button" type="submit">
                          Add activity
                        </button>
                        {activityConflictPreview && (
                          <div className="inline-error">
                            This slot overlaps with {activityConflictPreview.name}. Adjust the time before saving.
                          </div>
                        )}
                      </form>
                    </article>
                  </div>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen
                  description="Browse the auto-generated days, edit the schedule, and review route ordering for every stop."
                  meta={`${currentPlanActivities.length} activities`}
                  title="Day-by-day itinerary"
                >
                  <article className="panel-card">
                    <div className="itinerary-columns">
                      {selectedPlanDays.map((day) => (
                        <section className="day-card" key={day.id}>
                          <div className="day-card-header">
                            <div>
                              <strong>{day.date}</strong>
                              <small>
                                {(activitiesByDay.get(day.id) || []).length} scheduled activity
                                {(activitiesByDay.get(day.id) || []).length === 1 ? '' : 'ies'}
                              </small>
                            </div>
                            <span className="status-pill status-muted">
                              {calculateWorkload(activitiesByDay.get(day.id) || []).level}
                            </span>
                          </div>
                          <p className="day-support-copy">
                            {calculateWorkload(activitiesByDay.get(day.id) || []).message}
                          </p>

                          <div className="simple-list">
                            {(activitiesByDay.get(day.id) || []).map((activity) => (
                              <div className="itinerary-item" key={activity.id}>
                                <div>
                                  <strong>{activity.name}</strong>
                                  <p>{activity.locationName}</p>
                                  <small>
                                    {activity.timeslot} • {activity.startTime} → {activity.endTime} •{' '}
                                    Wait estimate {estimateWaitTime(activity)} min
                                  </small>
                                  <small>Created by user {activity.createdBy}</small>
                                </div>
                                <div className="inline-actions">
                                  <button
                                    className="ghost-button"
                                    onClick={() => handleActivityStatusChange(activity, 'DONE')}
                                    type="button"
                                  >
                                    Mark done
                                  </button>
                                  <button
                                    className="ghost-button danger"
                                    onClick={() => handleDeleteActivity(activity.id)}
                                    type="button"
                                  >
                                    Remove
                                  </button>
                                </div>
                              </div>
                            ))}
                          </div>

                          <div className="route-box">
                            <strong>Optimized route suggestion</strong>
                            <small>
                              {routeMetricsByDay[day.id]?.distanceKm} km •{' '}
                              {routeMetricsByDay[day.id]?.transferMinutes} min transfer time
                            </small>
                            <ol>
                              {(optimizedRoutesByDay[day.id] || []).map((activity) => (
                                <li key={activity.id}>
                                  {activity.name} — {activity.locationName}
                                </li>
                              ))}
                            </ol>
                            <p>{routeMetricsByDay[day.id]?.summary}</p>
                          </div>
                        </section>
                      ))}
                    </div>
                  </article>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen={false}
                  description="Understand expected queues and pick smarter visit windows for crowded attractions."
                  meta={`${waitTimeInsights.length} analyzed stops`}
                  title="Wait-time analysis"
                >
                  <article className="panel-card">
                    <div className="simple-list">
                      {waitTimeInsights.map((insight) => (
                        <div className="simple-row" key={insight.id}>
                          <div>
                            <strong>{insight.name}</strong>
                            <span>{insight.locationName}</span>
                          </div>
                          <small>
                            {insight.waitMinutes} min • {insight.bestWindow}
                          </small>
                        </div>
                      ))}
                      {waitTimeInsights.length === 0 && (
                        <p>Add activities to unlock wait-time analysis and visit-window guidance.</p>
                      )}
                    </div>
                  </article>
                </WorkspaceSection>
              </div>
            )}

            {activeTab === 'finance' && (
              <div className="workspace-stack">
                <div className="workspace-metric-grid">
                  <article className="panel-card">
                    <h3>Projected trip cost</h3>
                    <p className="metric-value">€{budgetProjection.estimatedTotal}</p>
                    <p>{budgetProjection.summary}</p>
                    <small>{budgetProjection.perDay} EUR per day across the selected trip.</small>
                  </article>

                  <article className="panel-card">
                    <h3>Booked reservation value</h3>
                    <p className="metric-value">€{budgetProjection.bookedReservationTotal}</p>
                    <p>{reservations.length} linked reservations</p>
                    <small>{premiumReservations.length} premium reservations above the active threshold.</small>
                  </article>

                  <article className="panel-card">
                    <h3>Budget balance</h3>
                    <p className="metric-value">
                      {budgetCoverage.difference >= 0 ? '+' : ''}
                      {budgetCoverage.difference}€
                    </p>
                    <p>Saved budget: €{budgetCoverage.savedBudget}</p>
                    <small>
                      {budgetCoverage.difference >= 0
                        ? 'You are currently above the forecast.'
                        : 'You are currently below the forecast.'}
                    </small>
                  </article>
                </div>

                <WorkspaceSection
                  defaultOpen
                  description="Control the total trip budget, compare it with the estimate, and keep day-to-day spending organized."
                  meta={`${budgets.length} budgets · ${expenses.length} expenses`}
                  title="Budget and expense control"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Budget management</h3>
                      <div className="finance-summary-card">
                        <strong>Estimated total</strong>
                        <span>€{budgetProjection.estimatedTotal}</span>
                        <small>
                          {budgetProjection.perDay} EUR per day • booked reservations €
                          {budgetProjection.bookedReservationTotal}
                        </small>
                      </div>
                      <form className="form-stack" onSubmit={handleCreateBudget}>
                        <label>
                          Total amount
                          <input
                            min="1"
                            required
                            type="number"
                            value={budgetForm.totalAmount}
                            onChange={(event) =>
                              setBudgetForm({ ...budgetForm, totalAmount: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Currency
                          <input
                            value={budgetForm.currency}
                            onChange={(event) =>
                              setBudgetForm({ ...budgetForm, currency: event.target.value })
                            }
                          />
                        </label>
                        <button className="primary-button" type="submit">
                          Save budget
                        </button>
                      </form>
                      <div className="simple-list">
                        {budgets.map((budget) => (
                          <div className="simple-row" key={budget.id}>
                            <div>
                              <strong>{budget.totalAmount}</strong>
                              <span>{budget.currency}</span>
                            </div>
                            <button
                              className="ghost-button danger"
                              onClick={() => handleDeleteBudget(budget.id)}
                              type="button"
                            >
                              Delete
                            </button>
                          </div>
                        ))}
                        {budgets.length === 0 && <p>No saved budgets yet for this trip.</p>}
                      </div>
                    </article>

                    <article className="panel-card">
                      <h3>Expense tracker</h3>
                      <div className="simple-list compact-list">
                        <div className="simple-row">
                          <strong>Dining projection</strong>
                          <span>€{budgetProjection.diningProjection}</span>
                        </div>
                        <div className="simple-row">
                          <strong>Mobility projection</strong>
                          <span>€{budgetProjection.mobilityProjection}</span>
                        </div>
                        <div className="simple-row">
                          <strong>Activity projection</strong>
                          <span>€{budgetProjection.activityProjection}</span>
                        </div>
                        <div className="simple-row">
                          <strong>Contingency reserve</strong>
                          <span>€{budgetProjection.contingency}</span>
                        </div>
                      </div>
                      <form className="form-stack" onSubmit={handleCreateExpense}>
                        <label>
                          Amount
                          <input
                            min="1"
                            required
                            type="number"
                            value={expenseForm.amount}
                            onChange={(event) =>
                              setExpenseForm({ ...expenseForm, amount: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Category
                          <input
                            required
                            value={expenseForm.category}
                            onChange={(event) =>
                              setExpenseForm({ ...expenseForm, category: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Date and time
                          <input
                            required
                            type="datetime-local"
                            value={expenseForm.date}
                            onChange={(event) =>
                              setExpenseForm({ ...expenseForm, date: event.target.value })
                            }
                          />
                        </label>
                        <button className="secondary-button" type="submit">
                          Add expense
                        </button>
                      </form>
                      <div className="simple-list">
                        {expenses.map((expense) => (
                          <div className="simple-row" key={expense.id}>
                            <div>
                              <strong>{expense.amount}</strong>
                              <span>{expense.category}</span>
                            </div>
                            <button
                              className="ghost-button danger"
                              onClick={() => handleDeleteExpense(expense.id)}
                              type="button"
                            >
                              Delete
                            </button>
                          </div>
                        ))}
                        {expenses.length === 0 && <p>No expenses logged yet for this trip.</p>}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen
                  description="Handle direct reservations and keep the asynchronous saga flow separate from the core budget editor."
                  meta={`${reservations.length + planReservations.length + sagaReservations.length} reservation records`}
                  title="Reservations and async booking flow"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Link a reservation</h3>
                      <form className="form-stack" onSubmit={handleCreateReservation}>
                        <label>
                          Reservation type
                          <input
                            required
                            value={reservationForm.type}
                            onChange={(event) =>
                              setReservationForm({ ...reservationForm, type: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Details
                          <textarea
                            rows="2"
                            value={reservationForm.details}
                            onChange={(event) =>
                              setReservationForm({ ...reservationForm, details: event.target.value })
                            }
                          />
                        </label>
                        <div className="split-fields">
                          <label>
                            Start date
                            <input
                              required
                              type="datetime-local"
                              value={reservationForm.startDate}
                              onChange={(event) =>
                                setReservationForm({
                                  ...reservationForm,
                                  startDate: event.target.value,
                                })
                              }
                            />
                          </label>
                          <label>
                            End date
                            <input
                              required
                              type="datetime-local"
                              value={reservationForm.endDate}
                              onChange={(event) =>
                                setReservationForm({
                                  ...reservationForm,
                                  endDate: event.target.value,
                                })
                              }
                            />
                          </label>
                        </div>
                        <label>
                          Price
                          <input
                            type="number"
                            value={reservationForm.price}
                            onChange={(event) =>
                              setReservationForm({ ...reservationForm, price: event.target.value })
                            }
                          />
                        </label>
                        <button className="primary-button" type="submit">
                          Link reservation
                        </button>
                      </form>
                    </article>

                    <article className="panel-card">
                      <h3>Run async saga reservation</h3>
                      <form className="form-stack" onSubmit={handleStartSagaReservation}>
                        <label>
                          Saga item name
                          <input
                            required
                            value={sagaRequestForm.itemName}
                            onChange={(event) =>
                              setSagaRequestForm({ ...sagaRequestForm, itemName: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Reservation type
                          <input
                            required
                            value={sagaRequestForm.reservationType}
                            onChange={(event) =>
                              setSagaRequestForm({
                                ...sagaRequestForm,
                                reservationType: event.target.value,
                              })
                            }
                          />
                        </label>
                        <div className="split-fields">
                          <label>
                            Start date
                            <input
                              required
                              type="date"
                              value={sagaRequestForm.startDate}
                              onChange={(event) =>
                                setSagaRequestForm({
                                  ...sagaRequestForm,
                                  startDate: event.target.value,
                                })
                              }
                            />
                          </label>
                          <label>
                            End date
                            <input
                              required
                              type="date"
                              value={sagaRequestForm.endDate}
                              onChange={(event) =>
                                setSagaRequestForm({
                                  ...sagaRequestForm,
                                  endDate: event.target.value,
                                })
                              }
                            />
                          </label>
                        </div>
                        <label>
                          Amount
                          <input
                            required
                            type="number"
                            value={sagaRequestForm.amount}
                            onChange={(event) =>
                              setSagaRequestForm({ ...sagaRequestForm, amount: event.target.value })
                            }
                          />
                        </label>
                        <div className="toggle-row">
                          <label>
                            <input
                              checked={sagaRequestForm.simulateFinanceFailure}
                              type="checkbox"
                              onChange={(event) =>
                                setSagaRequestForm({
                                  ...sagaRequestForm,
                                  simulateFinanceFailure: event.target.checked,
                                })
                              }
                            />
                            Simulate finance failure
                          </label>
                          <label>
                            <input
                              checked={sagaRequestForm.simulatePlanningFinalizationFailure}
                              type="checkbox"
                              onChange={(event) =>
                                setSagaRequestForm({
                                  ...sagaRequestForm,
                                  simulatePlanningFinalizationFailure: event.target.checked,
                                })
                              }
                            />
                            Simulate planning finalization failure
                          </label>
                        </div>
                        <button className="secondary-button" type="submit">
                          Start saga reservation
                        </button>
                      </form>
                    </article>
                  </div>

                  <div className="triple-grid">
                    <div className="panel-slab">
                      <h4>Linked reservations</h4>
                      {reservations.map((reservation) => (
                        <div className="simple-row" key={reservation.id}>
                          <div>
                            <strong>{reservation.type}</strong>
                            <span>{reservation.status}</span>
                          </div>
                          <button
                            className="ghost-button danger"
                            onClick={() => handleDeleteReservation(reservation.id)}
                            type="button"
                          >
                            Delete
                          </button>
                        </div>
                      ))}
                      {reservations.length === 0 && <p>No linked reservations yet.</p>}
                    </div>
                    <div className="panel-slab">
                      <h4>Premium reservations</h4>
                      {premiumReservations.map((reservation) => (
                        <div className="simple-row" key={reservation.id}>
                          <strong>{reservation.type}</strong>
                          <span>{reservation.price}</span>
                        </div>
                      ))}
                      {premiumReservations.length === 0 && <p>No premium reservations matched the filter.</p>}
                    </div>
                    <div className="panel-slab">
                      <h4>Async statuses</h4>
                      {planReservations.map((reservation) => (
                        <div className="simple-row" key={reservation.id}>
                          <div>
                            <strong>{reservation.itemName}</strong>
                            <span>{reservation.status}</span>
                          </div>
                          <small>{reservation.failureReason || 'No failure reason'}</small>
                        </div>
                      ))}
                      {sagaReservations.map((reservation) => (
                        <div className="simple-row" key={reservation.id}>
                          <div>
                            <strong>Finance #{reservation.id}</strong>
                            <span>{reservation.status}</span>
                          </div>
                          <small>{reservation.failureReason || 'Finance side finalized cleanly'}</small>
                        </div>
                      ))}
                      {planReservations.length === 0 && sagaReservations.length === 0 && (
                        <p>No async reservation statuses yet.</p>
                      )}
                    </div>
                  </div>
                </WorkspaceSection>
              </div>
            )}

            {activeTab === 'collaboration' && (
              <div className="workspace-stack">
                <div className="workspace-metric-grid">
                  <article className="panel-card">
                    <h3>Members</h3>
                    <p className="metric-value">{memberships.length}</p>
                    <p>People currently attached to this trip.</p>
                  </article>

                  <article className="panel-card">
                    <h3>Total votes</h3>
                    <p className="metric-value">{collaborationMetrics.voteTotal}</p>
                    <p>Votes collected for itinerary decisions.</p>
                  </article>

                  <article className="panel-card">
                    <h3>Shared links</h3>
                    <p className="metric-value">{sharedLinks.length}</p>
                    <p>Share entries ready for review or distribution.</p>
                  </article>
                </div>

                <WorkspaceSection
                  defaultOpen
                  description="Invite collaborators and assign permissions without mixing members into the voting view."
                  meta={`${memberships.length} members`}
                  title="Members and roles"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Add a plan member</h3>
                      <form className="form-stack" onSubmit={handleCreateMembership}>
                        <label>
                          User
                          <select
                            required
                            value={membershipForm.userId}
                            onChange={(event) =>
                              setMembershipForm({ ...membershipForm, userId: event.target.value })
                            }
                          >
                            <option value="">Select user</option>
                            {users.map((user) => (
                              <option key={user.id} value={user.id}>
                                {user.username}
                              </option>
                            ))}
                          </select>
                        </label>
                        <label>
                          Role
                          <select
                            value={membershipForm.role}
                            onChange={(event) =>
                              setMembershipForm({ ...membershipForm, role: event.target.value })
                            }
                          >
                            <option value="EDITOR">Editor</option>
                            <option value="VIEWER">Viewer</option>
                            <option value="OWNER">Owner</option>
                          </select>
                        </label>
                        <button className="primary-button" type="submit">
                          Add member
                        </button>
                      </form>
                    </article>

                    <article className="panel-card">
                      <h3>Current team</h3>
                      <div className="simple-list">
                        {memberships.map((membership) => (
                          <div className="simple-row" key={membership.id}>
                            <strong>
                              {users.find((user) => user.id === membership.userId)?.username ||
                                `User ${membership.userId}`}
                            </strong>
                            <span>{membership.role}</span>
                          </div>
                        ))}
                        {memberships.length === 0 && <p>No members added to this trip yet.</p>}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen
                  description="Track team participation and let the group push the best activities to the top."
                  meta={`${currentPlanActivities.length} activities available for voting`}
                  title="Decision-making and voting"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Collaboration health</h3>
                      <div className="simple-list">
                        <div className="simple-row">
                          <strong>Members</strong>
                          <span>{memberships.length}</span>
                        </div>
                        <div className="simple-row">
                          <strong>Total votes</strong>
                          <span>{collaborationMetrics.voteTotal}</span>
                        </div>
                        {collaborationMetrics.contributorRows.map((row) => (
                          <div className="simple-row" key={row.userId}>
                            <strong>{row.username}</strong>
                            <span>{row.count} activities created</span>
                          </div>
                        ))}
                        {collaborationMetrics.contributorRows.length === 0 && (
                          <p>Add activities with multiple members to show collaboration metrics.</p>
                        )}
                      </div>
                    </article>

                    <article className="panel-card">
                      <h3>Activity voting</h3>
                      <div className="voting-grid">
                        {currentPlanActivities.map((activity) => {
                          const activityVotes = votesByActivity[activity.id] || [];
                          const currentUserVote = activityVotes.find((vote) => vote.userId === currentUser.id);

                          return (
                            <div className="vote-card" key={activity.id}>
                              <div>
                                <strong>{activity.name}</strong>
                                <p>{activity.locationName}</p>
                                <small>{activityVotes.length} total votes</small>
                              </div>
                              <div className="inline-actions">
                                {currentUserVote ? (
                                  <button
                                    className="ghost-button danger"
                                    onClick={() => handleDeleteVote(currentUserVote.id, activity.id)}
                                    type="button"
                                  >
                                    Remove my vote
                                  </button>
                                ) : (
                                  <button
                                    className="secondary-button"
                                    onClick={() => handleCreateVote(activity.id)}
                                    type="button"
                                  >
                                    Vote for activity
                                  </button>
                                )}
                              </div>
                            </div>
                          );
                        })}
                        {currentPlanActivities.length === 0 && <p>No activities available for voting yet.</p>}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen={false}
                  description="Create shareable entries and keep public access links in one tidy place."
                  meta={`${sharedLinks.length} links`}
                  title="Sharing"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Create a shared link</h3>
                      <form className="form-stack" onSubmit={handleCreateSharedLink}>
                        <label>
                          Share URL
                          <input
                            required
                            value={sharedLinkForm.url}
                            onChange={(event) =>
                              setSharedLinkForm({ ...sharedLinkForm, url: event.target.value })
                            }
                          />
                        </label>
                        <label>
                          Link type
                          <input
                            required
                            value={sharedLinkForm.type}
                            onChange={(event) =>
                              setSharedLinkForm({ ...sharedLinkForm, type: event.target.value })
                            }
                          />
                        </label>
                        <button className="secondary-button" type="submit">
                          Generate shared entry
                        </button>
                      </form>
                    </article>

                    <article className="panel-card">
                      <h3>Active links</h3>
                      <div className="simple-list">
                        {sharedLinks.map((link) => (
                          <div className="simple-row" key={link.id}>
                            <a href={link.url} rel="noreferrer" target="_blank">
                              {link.url}
                            </a>
                            <button
                              className="ghost-button danger"
                              onClick={() => handleDeleteSharedLink(link.id)}
                              type="button"
                            >
                              Delete
                            </button>
                          </div>
                        ))}
                        {sharedLinks.length === 0 && <p>No shared links created for this trip yet.</p>}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>
              </div>
            )}

            {activeTab === 'notifications' && (
              <div className="workspace-stack">
                <div className="workspace-metric-grid">
                  <article className="panel-card">
                    <h3>Total reminders</h3>
                    <p className="metric-value">{notifications.length}</p>
                    <p>Saved notifications for this trip and this user.</p>
                  </article>

                  <article className="panel-card">
                    <h3>Upcoming alerts</h3>
                    <p className="metric-value">{upcomingNotifications.length}</p>
                    <p>Items currently closest to the current time.</p>
                  </article>

                  <article className="panel-card">
                    <h3>Reminder types</h3>
                    <p className="metric-value">
                      {new Set(notifications.map((notification) => notification.type)).size}
                    </p>
                    <p>Distinct categories used in the feed.</p>
                  </article>
                </div>

                <WorkspaceSection
                  defaultOpen
                  description="Create transport, reservation, or activity reminders without squeezing the feed."
                  meta={`${notifications.length} notifications`}
                  title="Reminder composer"
                >
                  <article className="panel-card">
                    <form className="form-stack" onSubmit={handleCreateNotification}>
                      <label>
                        Message
                        <input
                          required
                          value={notificationForm.message}
                          onChange={(event) =>
                            setNotificationForm({ ...notificationForm, message: event.target.value })
                          }
                        />
                      </label>
                      <label>
                        Date and time
                        <input
                          required
                          type="datetime-local"
                          value={notificationForm.date}
                          onChange={(event) =>
                            setNotificationForm({ ...notificationForm, date: event.target.value })
                          }
                        />
                      </label>
                      <label>
                        Type
                        <input
                          required
                          value={notificationForm.type}
                          onChange={(event) =>
                            setNotificationForm({ ...notificationForm, type: event.target.value })
                          }
                        />
                      </label>
                      <button className="primary-button" type="submit">
                        Save notification
                      </button>
                    </form>
                  </article>
                </WorkspaceSection>

                <WorkspaceSection
                  defaultOpen
                  description="Review what is coming next and keep a clean history of the reminders already saved."
                  meta={`${upcomingNotifications.length} upcoming`}
                  title="Reminder timeline"
                >
                  <div className="workspace-two-column">
                    <article className="panel-card">
                      <h3>Upcoming reminders</h3>
                      <div className="simple-list compact-list">
                        {upcomingNotifications.map((notification) => (
                          <div className="simple-row" key={`upcoming-${notification.id}`}>
                            <strong>Upcoming</strong>
                            <span>
                              {notification.message} • {notification.date}
                            </span>
                          </div>
                        ))}
                        {upcomingNotifications.length === 0 && <p>No upcoming reminders right now.</p>}
                      </div>
                    </article>

                    <article className="panel-card">
                      <h3>Notification feed</h3>
                      <div className="simple-list">
                        {notifications.map((notification) => (
                          <div className="simple-row" key={notification.id}>
                            <div>
                              <strong>{notification.message}</strong>
                              <span>
                                {notification.type} • {notification.date}
                              </span>
                            </div>
                            <button
                              className="ghost-button danger"
                              onClick={() => handleDeleteNotification(notification.id)}
                              type="button"
                            >
                              Delete
                            </button>
                          </div>
                        ))}
                        {notifications.length === 0 && (
                          <p>No notifications yet for the selected plan and current user.</p>
                        )}
                      </div>
                    </article>
                  </div>
                </WorkspaceSection>
              </div>
            )}
          </>
        ) : (
          <div className="state-panel">Please select a trip plan so we can show additional information.</div>
        )}
      </section>
    </div>
  );
}
