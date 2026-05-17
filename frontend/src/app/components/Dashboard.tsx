import { Link } from "react-router-dom";
import { 
  Plus, 
  Route, 
  Calendar, 
  MapPin, 
  Cloud, 
  DollarSign, 
  Users, 
  Vote, 
  Bookmark, 
  Download, 
  Bell, 
  Activity, 
  Utensils, 
  Share2, 
  Clock 
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

function formatDateRange(startDate, endDate) {
  const start = new Date(startDate);
  const end = new Date(endDate);

  return `${start.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })} - ${end.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" })}`;
}

export function Dashboard() {
  const { currentUser } = useAuth();
  const { plans, loading, error, activePlanId, setActivePlanId } = usePlanContext();

  const features = [
    { name: "Create Travel Plan", path: "/create-plan", icon: Plus, desc: "Start a new trip itinerary" },
    { name: "Route Optimization", path: "/route-optimization", icon: Route, desc: "Optimize visiting order" },
    { name: "Activity Scheduling", path: "/activity-scheduling", icon: Calendar, desc: "Schedule activities by time" },
    { name: "Attractions", path: "/attractions", icon: MapPin, desc: "Get personalized recommendations" },
    { name: "Weather Forecast", path: "/weather", icon: Cloud, desc: "Check destination weather" },
    { name: "Budget Management", path: "/budget", icon: DollarSign, desc: "Track trip expenses" },
    { name: "Collaborative Planning", path: "/collaborative", icon: Users, desc: "Plan with your group" },
    { name: "Activity Voting", path: "/voting", icon: Vote, desc: "Vote on group activities" },
    { name: "Reservations", path: "/reservations", icon: Bookmark, desc: "Manage bookings" },
    { name: "Offline Access", path: "/offline", icon: Download, desc: "Download itinerary PDF" },
    { name: "Notifications", path: "/notifications", icon: Bell, desc: "View reminders & alerts" },
    { name: "Schedule Load", path: "/schedule-load", icon: Activity, desc: "Check daily intensity" },
    { name: "Local Recommendations", path: "/local-recommendations", icon: Utensils, desc: "Nearby restaurants & events" },
    { name: "Share Plan", path: "/share", icon: Share2, desc: "Share your itinerary" },
    { name: "Waiting Times", path: "/waiting-times", icon: Clock, desc: "Best times to visit" },
  ];

  return (
    <div className="max-w-7xl mx-auto">
      {/* Hero Section */}
      <div className="bg-white border-2 border-gray-300 rounded-lg p-8 mb-6">
        <h1 className="text-3xl font-medium mb-2">Welcome{currentUser?.username ? `, ${currentUser.username}` : ""}</h1>
        <p className="text-gray-600 mb-4">
          Organize every part of your trip in one place, from planning and scheduling to bookings, sharing, and group decisions.
        </p>
        <Link
          to="/create-plan"
          className="inline-flex items-center gap-2 px-6 py-3 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600"
        >
          <Plus className="w-5 h-5" />
          Create New Travel Plan
        </Link>
      </div>

      {/* Current Trips */}
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <h2 className="text-xl font-medium mb-4">Your Travel Plans</h2>
        {loading ? <ModuleLoading label="Loading your travel plans..." /> : null}
        {!loading && error ? <ModuleError message={error} /> : null}
        {!loading && !error && plans.length === 0 ? (
          <ModuleEmpty
            title="No travel plans yet"
            description="Create your first travel plan to start building your itinerary, schedule, budget, and travel details."
            action={
              <Link
                to="/create-plan"
                className="inline-flex items-center gap-2 px-5 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600"
              >
                <Plus className="w-4 h-4" />
                Create your first plan
              </Link>
            }
          />
        ) : null}
        {!loading && !error && plans.length > 0 ? (
          <div className="space-y-3">
            {plans.map((trip) => (
              <button
                key={trip.id}
                onClick={() => setActivePlanId(trip.id)}
                className={`w-full text-left border rounded p-4 hover:bg-gray-50 ${
                  activePlanId === trip.id ? "border-blue-400 bg-blue-50" : "border-gray-300"
                }`}
              >
                <div className="flex justify-between items-start gap-4">
                  <div>
                    <h3 className="font-medium">{trip.name}</h3>
                    <p className="text-sm text-gray-600">{trip.destinationName || "Destination pending"}</p>
                    <p className="text-sm text-gray-500">{formatDateRange(trip.startDate, trip.endDate)}</p>
                  </div>
                  <span className={`px-3 py-1 text-sm border rounded ${
                    activePlanId === trip.id
                      ? "bg-blue-100 border-blue-400 text-blue-700"
                      : "bg-gray-100 border-gray-400 text-gray-700"
                  }`}>
                    {trip.status || "PLANNING"}
                  </span>
                </div>
              </button>
            ))}
          </div>
        ) : null}
      </div>

      {/* Features Grid */}
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
        <h2 className="text-xl font-medium mb-4">Features</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {features.map(feature => {
            const Icon = feature.icon;
            return (
              <Link
                key={feature.path}
                to={feature.path}
                className="border border-gray-300 rounded p-4 hover:bg-gray-50 hover:border-gray-400 transition-colors"
              >
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-gray-100 border border-gray-300 rounded flex items-center justify-center flex-shrink-0">
                    <Icon className="w-5 h-5 text-gray-700" />
                  </div>
                  <div>
                    <h3 className="font-medium text-sm mb-1">{feature.name}</h3>
                    <p className="text-xs text-gray-600">{feature.desc}</p>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}
