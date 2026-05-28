import { MapPin, AlertTriangle, CheckCircle, RefreshCw, Calendar, ChevronRight } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getRouteOptimization } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import {MapContainer, TileLayer, Marker, Popup, Polyline} from "react-leaflet";
import "leaflet/dist/leaflet.css";

// group stops by day and timeslot for easier display and analysis of how well the optimization clustered activities together in the same time periods, which can be a sign of a more efficient route
function groupStopsByDayAndTimeslot(stops: any[]) {
  const grouped: Record<string, Record<string, any[]>> = {};

  for (const stop of stops) {
    const day = stop.dayDate
      ? new Date(stop.dayDate).toLocaleDateString()
      : "Date pending";
    const slot = stop.timeslot || "Flexible";

    if (!grouped[day]) grouped[day] = {};
    if (!grouped[day][slot]) grouped[day][slot] = [];
    grouped[day][slot].push(stop);
  }

  return grouped;
}

// calculate distance in km between two lat/lng points using Haversine formula
function getDistanceKm(a: any, b: any): number {
  if (!a?.latitude || !b?.latitude) return 0;
  const R = 6371;
  const dLat = ((b.latitude - a.latitude) * Math.PI) / 180;
  const dLon = ((b.longitude - a.longitude) * Math.PI) / 180;
  const sinLat = Math.sin(dLat / 2);
  const sinLon = Math.sin(dLon / 2);
  const c =
    2 *
    Math.asin(
      Math.sqrt(
        sinLat * sinLat +
          Math.cos((a.latitude * Math.PI) / 180) *
            Math.cos((b.latitude * Math.PI) / 180) *
            sinLon * sinLon
      )
    );
  return R * c;
}

// check if any two consecutive stops are more than 5km apart, which may indicate a need for an additional stop or a warning about travel time between those stops
function getSlotWarning(stops: any[]): { hasWarning: boolean; maxKm: number } {
  if (stops.length < 2) return { hasWarning: false, maxKm: 0 };
  let maxKm = 0;
  for (let i = 0; i < stops.length - 1; i++) {
    const d = getDistanceKm(stops[i], stops[i + 1]);
    if (d > maxKm) maxKm = d;
  }
  return { hasWarning: maxKm > 5, maxKm };
}

function getTotalRouteDistance(stops: any[], start?: any): number {
  if (!stops.length) return 0;

  let total = 0;

  if (start) {
    total += getDistanceKm(start, stops[0]);
  }

  for (let i = 0; i < stops.length - 1; i++) {
    total += getDistanceKm(stops[i], stops[i + 1]);
  }

  return total;
}

const TIMESLOT_LABELS: Record<string, { label: string; icon: string }> = {
  MORNING: { label: "Morning", icon: "🌅" },
  morning: { label: "Morning", icon: "🌅" },
  AFTERNOON: { label: "Afternoon", icon: "☀️" },
  afternoon: { label: "Afternoon", icon: "☀️" },
  EVENING: { label: "Evening", icon: "🌙" },
  evening: { label: "Evening", icon: "🌙" },
  Flexible: { label: "Flexible", icon: "🕐" },
};

export function RouteOptimization() {
  const { activePlan } = usePlanContext();
  const [routeData, setRouteData] = useState<any>(null);
  const [currentLocation, setCurrentLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);
  const [startLocation, setStartLocation] = useState("");
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedDay, setSelectedDay] = useState<string | null>(null);
  const loadRouteData = async () => {
    if (!activePlan) {
      setRouteData(null);
      return;
    }

    try {
      setLoading(true);
      setError("");

     const nextRoute = await getRouteOptimization(activePlan.id);
     setRouteData(nextRoute);

      if (nextRoute?.stops?.length) {
        const firstDay = nextRoute.stops[0].dayDate
          ? new Date(nextRoute.stops[0].dayDate).toLocaleDateString()
          : "Date pending";
        setSelectedDay(firstDay);
      }

    } catch (fetchError) {

      setError(
        getApiErrorMessage(
          fetchError,
          "Unable to calculate route optimization."
        )
      );

    } finally {
      setLoading(false);
    }
    };
  
    const handleSetStartLocation = async () => {
      if (!startLocation.trim()) return;

      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
            startLocation
          )}`
        );

        const data = await response.json();

        if (!data.length) {
          alert("Location not found");
          return;
        }

        setCurrentLocation({
          latitude: parseFloat(data[0].lat),
          longitude: parseFloat(data[0].lon),
        });

      } catch (error) {
        console.error(error);
      }
    };

  useEffect(() => {
    loadRouteData();
  }, [activePlan?.id]);

  useEffect(() => {
    const fetchSuggestions = async () => {

      if (startLocation.trim().length < 2) {
        setSuggestions([]);
        return;
      }

      try {

        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
            startLocation
          )}&limit=5`
        );

        const data = await response.json();

        setSuggestions(data);

      } catch (error) {
        console.error(error);
      }
    };

    const timeout = setTimeout(fetchSuggestions, 300);

    return () => clearTimeout(timeout);

  }, [startLocation]);

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
    const allDays = routeData?.stops
    ? [...new Set(
        routeData.stops.map((s: any) =>
          s.dayDate ? new Date(s.dayDate).toLocaleDateString() : "Date pending"
        )
      )]
    : [];

  const stopsForSelectedDay = routeData?.stops?.filter((s: any) => {
    const day = s.dayDate
      ? new Date(s.dayDate).toLocaleDateString()
      : "Date pending";
    return day === selectedDay;
  }) ?? [];

  const totalDistance = getTotalRouteDistance(
  stopsForSelectedDay,
  currentLocation
);

const routeWarning = totalDistance > 5;

const routePoints = [
  ...(currentLocation
    ? [[currentLocation.latitude, currentLocation.longitude]]
    : []),

  ...stopsForSelectedDay.map((stop: any) => [
    stop.latitude,
    stop.longitude,
  ]),
];

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

        {/* LEFT PANEL */}
        <div className="bg-white border-2 border-gray-300 rounded-lg p-6 space-y-5">

          {/* Day tabs */}
          <div>
            <h2 className="text-xl font-medium mb-3 flex items-center gap-2">
              <Calendar className="w-5 h-5" />
              Schedule Analysis
            </h2>

            {allDays.length > 0 ? (
              <div className="flex flex-wrap gap-2 mb-4">
                {allDays.map((day: string) => (
                  <button
                    key={String(day)}
                    onClick={() => setSelectedDay(String(day))}
                    className={`px-3 py-1.5 rounded-full text-sm border-2 transition-colors ${
                      selectedDay === day
                        ? "bg-blue-500 text-white border-blue-500"
                        : "bg-white text-gray-700 border-gray-300 hover:border-blue-400"
                    }`}
                  >
                    {day}
                  </button>
                ))}
              </div>
            ) : (
              <ModuleEmpty
                title="No activities yet"
                description="Add activities to your plan, then refresh to see the analysis."
              />
            )}
          </div>

          {selectedDay && stopsForSelectedDay.length > 0 && (
            <div className="space-y-4">

              <div className="border border-gray-300 rounded-lg p-4 bg-gray-50">
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-medium text-sm">
                    Daily Route
                  </h3>

                  <span
                    className={`text-xs font-medium px-2 py-1 rounded-full ${
                      routeWarning
                        ? "bg-orange-100 text-orange-700"
                        : "bg-green-100 text-green-700"
                    }`}
                  >
                    {routeWarning ? "Needs optimization" : "Efficient"}
                  </span>
                </div>

                <div className="space-y-2">

                  {currentLocation && (
                    <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded p-3">
                      <div className="w-6 h-6 rounded-full bg-red-500 text-white text-xs flex items-center justify-center font-medium">
                        S
                      </div>

                      <div>
                        <p className="text-sm font-medium">
                          Starting Location
                        </p>

                        <p className="text-xs text-gray-600">
                          {startLocation}
                        </p>
                      </div>
                    </div>
                  )}

                  {stopsForSelectedDay.map((stop: any, idx: number) => (
                    <div
                      key={`${stop.activityId}-${idx}`}
                      className="flex items-start gap-3 bg-white border border-gray-200 rounded p-3"
                    >
                      <div className="w-6 h-6 rounded-full bg-blue-500 text-white text-xs flex items-center justify-center font-medium">
                        {idx + 1}
                      </div>

                      <div className="flex-1">
                        <p className="text-sm font-medium">
                          {stop.activityName || stop.locationName}
                        </p>

                        <p className="text-xs text-gray-500">
                          {stop.address || stop.locationName}
                        </p>

                        <p className="text-xs text-blue-600 mt-1">
                          {stop.timeslot}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {routeWarning && (
                <div className="border border-orange-300 bg-orange-50 rounded-lg p-4">
                  <div className="flex items-start gap-2">
                    <AlertTriangle className="w-5 h-5 text-orange-600 mt-0.5" />

                    <div>
                      <p className="text-sm font-medium text-orange-800">
                        This route contains inefficient travel segments
                      </p>

                      <p className="text-xs text-orange-700 mt-1">
                        Total route distance is approximately{" "}
                        {totalDistance.toFixed(1)} km.
                      </p>

                      <button className="mt-3 px-3 py-2 bg-orange-500 hover:bg-orange-600 text-white text-sm rounded">
                        Show optimized route
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Starting location + refresh */}
          <div className="border-t border-gray-300 pt-4 space-y-3">
            <div className="relative">
              <label className="block text-sm font-medium mb-1">Starting location</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. Hotel Roma, Via Veneto..."
                  value={startLocation}
                  onChange={(e) => setStartLocation(e.target.value)}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded text-sm"
                />
                <button
                  onClick={handleSetStartLocation}
                  className="px-4 py-2 bg-blue-500 text-white rounded text-sm hover:bg-blue-600"
                >
                  Set
                </button>
              </div>

              {suggestions.length > 0 && (
                <div className="absolute z-50 w-full bg-white border border-gray-300 rounded mt-1 shadow-lg max-h-60 overflow-auto">
                  {suggestions.map((suggestion, index) => (
                    <button
                      key={index}
                      onClick={() => {
                        setStartLocation(suggestion.display_name);
                        setCurrentLocation({
                          latitude: parseFloat(suggestion.lat),
                          longitude: parseFloat(suggestion.lon),
                        });
                        setSuggestions([]);
                      }}
                      className="w-full text-left px-3 py-2 hover:bg-gray-100 border-b border-gray-100 text-sm"
                    >
                      {suggestion.display_name}
                    </button>
                  ))}
                </div>
              )}
            </div>

            <button
              onClick={loadRouteData}
              className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 flex items-center justify-center gap-2 text-sm"
            >
              <RefreshCw className="w-4 h-4" />
              Refresh
            </button>
          </div>
        </div>

        {/* RIGHT PANEL */}
        <div className="space-y-4">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-3 flex items-center gap-2">
              <MapPin className="w-5 h-5" />
              Route Map
              {selectedDay && (
                <span className="text-sm font-normal text-gray-500 ml-1">— {selectedDay}</span>
              )}
            </h2>

            <div className="border-2 border-gray-300 rounded overflow-hidden">
              <MapContainer
                center={
                  currentLocation
                    ? [currentLocation.latitude, currentLocation.longitude]
                    : stopsForSelectedDay[0]
                    ? [stopsForSelectedDay[0].latitude, stopsForSelectedDay[0].longitude]
                    : [48.8584, 2.2945]
                }
                zoom={13}
                className="h-80 w-full"
              >
                <TileLayer
                  attribution="&copy; OpenStreetMap contributors"
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                {currentLocation && (
                  <Marker position={[currentLocation.latitude, currentLocation.longitude]}>
                    <Popup>Starting location</Popup>
                  </Marker>
                )}

                {stopsForSelectedDay.map((stop: any, index: number) => (
                  <Marker
                    key={`${stop.locationId}-${index}`}
                    position={[stop.latitude, stop.longitude]}
                  >
                    <Popup>
                      <p className="font-semibold">{stop.activityName || stop.locationName}</p>
                      <p className="text-xs text-gray-500">{stop.timeslot}</p>
                    </Popup>
                  </Marker>
                ))}
              </MapContainer>
            </div>

            <div className="grid grid-cols-3 gap-3 mt-4">

              <div className="border border-gray-300 rounded p-3 text-center">
                <p className="text-2xl font-medium">
                  {stopsForSelectedDay.length}
                </p>

                <p className="text-xs text-gray-600">
                  Activities
                </p>
              </div>

              <div className="border border-gray-300 rounded p-3 text-center">
                <p className="text-2xl font-medium">
                  {totalDistance.toFixed(1)}
                </p>

                <p className="text-xs text-gray-600">
                  Total km
                </p>
              </div>

              <div
                className={`border rounded p-3 text-center ${
                  routeWarning
                    ? "border-orange-300 bg-orange-50"
                    : "border-green-300 bg-green-50"
                }`}
              >
                <p
                  className={`text-2xl font-medium ${
                    routeWarning
                      ? "text-orange-700"
                      : "text-green-700"
                  }`}
                >
                  {routeWarning ? "!" : "✓"}
                </p>

                <p
                  className={`text-xs ${
                    routeWarning
                      ? "text-orange-700"
                      : "text-green-700"
                  }`}
                >
                  {routeWarning ? "Needs optimization" : "Efficient"}
                </p>
              </div>

            </div>
          </div>
        </div>

      </div>
    </div>
  );
}

