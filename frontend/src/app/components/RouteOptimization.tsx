import { Navigation, MapPin, Clock, TrendingDown, RefreshCw } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getLocationsByDestination } from "../../api/locationService";
import { getRouteOptimization } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function RouteOptimization() {
  const { activePlan } = usePlanContext();
  const [locations, setLocations] = useState([]);
  const [routeData, setRouteData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const loadRouteData = async () => {
    if (!activePlan) {
      setLocations([]);
      setRouteData(null);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const [nextLocations, nextRoute] = await Promise.all([
        activePlan.destinationId ? getLocationsByDestination(activePlan.destinationId) : Promise.resolve([]),
        getRouteOptimization(activePlan.id),
      ]);
      setLocations(nextLocations);
      setRouteData(nextRoute);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to calculate route optimization."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRouteData();
  }, [activePlan?.id]);

  if (!activePlan) {
    return (
      <ModuleEmpty
        title="No active plan selected"
        description="Select or create a travel plan first so route optimization can use its destination and activities."
      />
    );
  }

  if (loading) {
    return <ModuleLoading label="Optimizing route..." />;
  }

  if (error) {
    return <ModuleError message={error} />;
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
          <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
            <MapPin className="w-5 h-5" />
            Destination Locations
          </h2>

          {locations.length === 0 ? (
            <ModuleEmpty
              title="No mapped locations"
              description="This destination does not have saved locations yet, so route optimization can only use activities already attached to the plan."
            />
          ) : (
            <div className="space-y-3 mb-6">
              {locations.map((location) => (
                <div key={location.id} className="border border-gray-300 rounded p-3">
                  <div className="flex items-start gap-3">
                    <input type="checkbox" checked readOnly className="mt-1 w-4 h-4" />
                    <div className="flex-1">
                      <p className="font-medium text-sm">{location.name}</p>
                      <p className="text-xs text-gray-600">{location.address || "Address unavailable"}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div className="border-t border-gray-300 pt-4 space-y-3">
            <div>
              <label className="block text-sm font-medium mb-2">Optimization Strategy</label>
              <input
                readOnly
                value={routeData?.strategy || "Distance-based ordering"}
                className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-50"
              />
            </div>

            <button
              onClick={loadRouteData}
              className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 flex items-center justify-center gap-2"
            >
              <RefreshCw className="w-4 h-4" />
              Refresh Route
            </button>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <Navigation className="w-5 h-5" />
              Optimized Route Map
            </h2>

            <div className="border-2 border-gray-300 rounded bg-gray-50 h-80 flex items-center justify-center relative overflow-hidden">
              <div className="absolute inset-0 p-4">
                <div
                  className="w-full h-full"
                  style={{
                    backgroundImage:
                      "linear-gradient(#ddd 1px, transparent 1px), linear-gradient(90deg, #ddd 1px, transparent 1px)",
                    backgroundSize: "20px 20px",
                  }}
                />
              </div>
              <div className="relative z-10 text-gray-500 text-sm flex flex-col items-center gap-2">
                <MapPin className="w-8 h-8" />
                <span>{routeData?.destinationName || activePlan.destinationName}</span>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3 mt-4">
              <div className="border border-gray-300 rounded p-3 text-center">
                <p className="text-2xl font-medium">{routeData?.stops?.length || 0}</p>
                <p className="text-xs text-gray-600">Suggested Stops</p>
              </div>
              <div className="border border-gray-300 rounded p-3 text-center">
                <p className="text-2xl font-medium">{routeData?.strategy || "Auto"}</p>
                <p className="text-xs text-gray-600">Strategy</p>
              </div>
              <div className="border border-gray-300 rounded p-3 text-center bg-green-50 border-green-300">
                <p className="text-2xl font-medium text-green-700">
                  {routeData?.totalDistanceScore?.toFixed(1) || "0.0"}
                </p>
                <p className="text-xs text-green-700">Distance Score</p>
              </div>
            </div>
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <TrendingDown className="w-5 h-5" />
              Suggested Visit Order
            </h2>

            {routeData?.stops?.length ? (
              <div className="space-y-3">
                {routeData.stops.map((stop, index) => (
                  <div key={`${stop.activityId}-${stop.locationId}-${index}`} className="border border-gray-300 rounded p-3">
                    <div className="flex items-start gap-3">
                      <div className="w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium border-2 bg-blue-100 border-blue-400 text-blue-700">
                        {stop.suggestedOrder}
                      </div>
                      <div className="flex-1">
                        <div className="flex justify-between items-start mb-1 gap-3">
                          <h3 className="font-medium text-sm">{stop.activityName || stop.locationName}</h3>
                          <span className="text-xs text-gray-600 flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {stop.timeslot || "Flexible"}
                          </span>
                        </div>
                        <div className="text-xs text-gray-600 space-y-1">
                          <p>{stop.locationName}</p>
                          <p>{stop.address || "Address unavailable"}</p>
                          <p>{stop.dayDate ? new Date(stop.dayDate).toLocaleDateString() : "Date pending"}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <ModuleEmpty
                title="No route suggestions yet"
                description="Add activities and locations to your active plan, then refresh this module to see optimized ordering."
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
