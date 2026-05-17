import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getDestinations } from "../../api/destinationService";
import { getTravelPlans } from "../../api/planService";
import { useAuth } from "./AuthContext";

const ACTIVE_PLAN_STORAGE_KEY = "active-plan-id";
const PlanContext = createContext(null);

function readStoredPlanId() {
  const value = localStorage.getItem(ACTIVE_PLAN_STORAGE_KEY);
  return value ? Number(value) : null;
}

export function PlanProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [plans, setPlans] = useState([]);
  const [destinations, setDestinations] = useState([]);
  const [activePlanId, setActivePlanIdState] = useState(readStoredPlanId);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const setActivePlanId = (planId) => {
    setActivePlanIdState(planId);
    if (planId) {
      localStorage.setItem(ACTIVE_PLAN_STORAGE_KEY, String(planId));
    } else {
      localStorage.removeItem(ACTIVE_PLAN_STORAGE_KEY);
    }
  };

  const refreshAppData = async () => {
    if (!isAuthenticated) {
      setPlans([]);
      setDestinations([]);
      setActivePlanId(null);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const [nextPlans, nextDestinations] = await Promise.all([
        getTravelPlans(),
        getDestinations(),
      ]);

      setPlans(nextPlans);
      setDestinations(nextDestinations);

      if (nextPlans.length === 0) {
        setActivePlanId(null);
      } else {
        const storedId = readStoredPlanId();
        const matchingStoredPlan = nextPlans.find((plan) => plan.id === storedId);
        setActivePlanId(matchingStoredPlan ? matchingStoredPlan.id : nextPlans[0].id);
      }
    } catch (fetchError) {
      setError(fetchError.response?.data?.message || "Unable to load plans right now.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAppData();
  }, [isAuthenticated]);

  const value = useMemo(
    () => ({
      plans,
      destinations,
      loading,
      error,
      activePlanId,
      activePlan: plans.find((plan) => plan.id === activePlanId) || null,
      refreshAppData,
      refreshPlans: refreshAppData,
      setActivePlanId,
    }),
    [activePlanId, destinations, error, loading, plans],
  );

  return <PlanContext.Provider value={value}>{children}</PlanContext.Provider>;
}

export function usePlanContext() {
  const context = useContext(PlanContext);

  if (!context) {
    throw new Error("usePlanContext must be used inside PlanProvider");
  }

  return context;
}
