import { Link, Outlet, useLocation } from "react-router-dom";
import { 
  Home, 
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
  Clock,
  LogOut,
  Menu
} from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";

export function Layout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const { currentUser, logout } = useAuth();
  const { activePlan, activePlanId, plans, setActivePlanId } = usePlanContext();

  const navigation = [
    { name: "Dashboard", path: "/", icon: Home },
    { name: "Create Travel Plan", path: "/create-plan", icon: Plus },
    { name: "Route Optimization", path: "/route-optimization", icon: Route },
    { name: "Activity Scheduling", path: "/activity-scheduling", icon: Calendar },
    { name: "Attractions", path: "/attractions", icon: MapPin },
    { name: "Weather Forecast", path: "/weather", icon: Cloud },
    { name: "Budget Management", path: "/budget", icon: DollarSign },
    { name: "Collaborative Planning", path: "/collaborative", icon: Users },
    { name: "Activity Voting", path: "/voting", icon: Vote },
    { name: "Reservations", path: "/reservations", icon: Bookmark },
    { name: "Offline Access", path: "/offline", icon: Download },
    { name: "Notifications", path: "/notifications", icon: Bell },
    { name: "Schedule Load", path: "/schedule-load", icon: Activity },
    { name: "Local Recommendations", path: "/local-recommendations", icon: Utensils },
    { name: "Share Plan", path: "/share", icon: Share2 },
    { name: "Waiting Times", path: "/waiting-times", icon: Clock },
  ];

  const initials = currentUser?.username?.slice(0, 2)?.toUpperCase() || "ST";

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside 
        className={`${
          sidebarOpen ? "w-64" : "w-20"
        } bg-white border-r border-gray-300 transition-all duration-300 flex flex-col`}
      >
        {/* Header */}
        <div className="h-16 border-b border-gray-300 flex items-center justify-between px-4">
          {sidebarOpen && (
            <h1 className="text-lg font-medium">Smart Travel Planner</h1>
          )}
          <button 
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-2 hover:bg-gray-100 rounded"
          >
            <Menu className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto py-4">
          {navigation.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 mx-2 rounded transition-colors ${
                  isActive 
                    ? "bg-blue-100 border border-blue-400 text-blue-700" 
                    : "hover:bg-gray-100 border border-transparent"
                }`}
                title={item.name}
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                {sidebarOpen && (
                  <span className="text-sm">{item.name}</span>
                )}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Navigation Bar */}
        <header className="h-16 bg-white border-b border-gray-300 flex items-center justify-between px-6">
          <div className="flex items-center gap-4">
            <h2 className="text-xl font-medium">
              {navigation.find(n => n.path === location.pathname)?.name || "Dashboard"}
            </h2>
          </div>
          <div className="flex items-center gap-4">
            <select
              value={activePlanId || ""}
              onChange={(event) => setActivePlanId(event.target.value ? Number(event.target.value) : null)}
              className="px-4 py-2 border border-gray-300 rounded bg-white text-sm"
            >
              {plans.length === 0 ? <option value="">No plans yet</option> : null}
              {plans.map((plan) => (
                <option key={plan.id} value={plan.id}>
                  {plan.name}
                </option>
              ))}
            </select>
            <div className="text-right hidden md:block">
              <p className="text-sm font-medium">{currentUser?.username || "Traveler"}</p>
              <p className="text-xs text-gray-500">{activePlan?.destinationName || "Create or select a plan"}</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-gray-200 border-2 border-gray-400 flex items-center justify-center text-sm font-medium">
              {initials}
            </div>
            <button
              onClick={handleLogout}
              className="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50 flex items-center gap-2 text-sm"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto bg-gray-50 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
