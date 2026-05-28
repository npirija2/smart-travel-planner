import { MapPin, AlertTriangle, Calendar } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getRouteOptimization } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import {MapContainer, TileLayer, Marker, Popup, Polyline, ZoomControl} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
const redMarker = new L.Icon({
  iconUrl:
    "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",

  shadowUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",

  iconSize: [25, 41],
  iconAnchor: [12, 41],
});
function createNumberedIcon(
  number: number,
  color: string
) {
  return L.divIcon({
    className: "",
    html: `
      <div style="
        background:${color};
        width:28px;
        height:28px;
        border-radius:50%;
        display:flex;
        align-items:center;
        justify-content:center;
        color:white;
        font-weight:bold;
        border:2px solid white;
        box-shadow:0 2px 6px rgba(0,0,0,0.3);
      ">
        ${number}
      </div>
    `,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

function assignTimeslotByOrder(index: number, total: number) {
  const percentage = index / total;

  if (percentage < 0.34) return "MORNING";
  if (percentage < 0.67) return "AFTERNOON";

  return "EVENING";
}

function offsetCoordinates(
  lat: number,
  lng: number,
  index: number,
  stops: any[]
) {
  const duplicatesBefore = stops.slice(0, index).filter(
    (s: any) =>
      s.latitude === stops[index].latitude &&
      s.longitude === stops[index].longitude
  ).length;

  const offset = duplicatesBefore * 0.005;

  return [
    lat + offset,
    lng + offset,
  ];
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
  const [showOptimizedRoute, setShowOptimizedRoute] = useState(false);
  const [transportMode, setTransportMode] = useState<"driving" | "walking" | "cycling">("driving");
  const [routeGeometry, setRouteGeometry] = useState<any[]>([]);
  const [routeLegs, setRouteLegs] = useState<any[]>([]);
  const [routeDistanceKm, setRouteDistanceKm] = useState(0);
  const [optimizedStopsState, setOptimizedStopsState] = useState<any[]>([]);
  const [originalRouteKm, setOriginalRouteKm] = useState(0);
  const allDays = routeData?.stops
      ? [
          ...new Set(
            routeData.stops.map((s: any) =>
              s.dayDate
                ? new Date(s.dayDate).toLocaleDateString()
                : "Date pending"
            )
          ),
        ].sort((a: string, b: string) => {
          const [dayA, monthA, yearA] = a.split("/");
          const [dayB, monthB, yearB] = b.split("/");

          return (
            new Date(
              Number(yearA),
              Number(monthA) - 1,
              Number(dayA)
            ).getTime() -
            new Date(
              Number(yearB),
              Number(monthB) - 1,
              Number(dayB)
            ).getTime()
          );
        })
      : [];

    const stopsForSelectedDay = routeData?.stops?.filter((s: any) => {
      const day = s.dayDate
        ? new Date(s.dayDate).toLocaleDateString()
        : "Date pending";
      return day === selectedDay;
    }) ?? [];
  useEffect(() => {
    const optimizeRoute = async () => {
      if (
        !currentLocation ||
        stopsForSelectedDay.length === 0
      ) {
        setOptimizedStopsState([]);
        return;
      }

      try {
        const allPoints = [
          currentLocation,
          ...stopsForSelectedDay,
        ];

        const coordinates = allPoints
          .map(
            (p: any) =>
              `${p.longitude},${p.latitude}`
          )
          .join(";");

        const response = await fetch(
          `https://router.project-osrm.org/trip/v1/${transportMode}/${coordinates}?source=first&roundtrip=false`
        );

        const data = await response.json();

        if (!data.trips?.length) return;

        const waypointOrder =
          data.waypoints
            .slice(1)
            .map((w: any) => w.waypoint_index - 1);

        const reordered =
          waypointOrder.map(
            (index: number) =>
              stopsForSelectedDay[index]
          );

        setOptimizedStopsState(reordered);

      } catch (err) {
        console.error(err);
      }
    };

    optimizeRoute();
  }, [
    currentLocation,
    stopsForSelectedDay,
    transportMode,
  ]);

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
    
    
  useEffect(() => {
  loadRouteData();
  }, [activePlan?.id]);

  
  useEffect(() => {
    setCurrentLocation(null);
    setStartLocation("");
    setShowOptimizedRoute(false);
  }, [selectedDay]);

  useEffect(() => {
    const fetchRoute = async () => {
      const points = (
        showOptimizedRoute
          ? optimizedRoutePoints
          : originalRoutePoints
      ) as number[][];

      if (points.length < 2) {
        setRouteGeometry([]);
        return;
      }

      try {
        const coordinates = points
          .map((p) => `${p[1]},${p[0]}`)
          .join(";");

        const response = await fetch(`https://router.project-osrm.org/route/v1/${transportMode}/${coordinates}?overview=full&geometries=geojson&steps=true`);
        const data = await response.json();

        const geometry =
          data.routes?.[0]?.geometry?.coordinates?.map(
            (coord: number[]) => [
              coord[1],
              coord[0],
            ]
          ) || [];

        setRouteGeometry(geometry);
        setRouteLegs(data.routes?.[0]?.legs || []);
        setRouteDistanceKm(data.routes?.[0]?.distance ? data.routes[0].distance / 1000 : 0);
        if (!showOptimizedRoute) {
          setOriginalRouteKm(
            data.routes?.[0]?.distance
              ? data.routes[0].distance / 1000
              : 0
          );
        }
      } catch (err) {
        console.error(err);
      }
    };

    fetchRoute();
  }, [
    showOptimizedRoute,
    currentLocation,
    selectedDay,
    transportMode,
  ]);
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
  const routeWarning =
    routeDistanceKm > 25 &&
    stopsForSelectedDay.length >= 4;

  const originalRoutePoints = [
    ...(currentLocation
      ? [[currentLocation.latitude, currentLocation.longitude]]
      : []),

    ...stopsForSelectedDay.map((stop: any) => [
      stop.latitude,
      stop.longitude,
    ]),
  ];
  const optimizedRoutePoints  = [
    ...(currentLocation
      ? [[currentLocation.latitude, currentLocation.longitude]]
      : []),

    ...optimizedStopsState.map((stop: any) => [
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
              Daily Route Optimization
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
          {/* Route starting point */}
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
          </div>
          <div className="border-t border-gray-300 pt-4">
            <label className="block text-sm font-medium mb-2">
              Transport mode
            </label>

            <div className="flex gap-2">
              {[
                { key: "driving", label: "Driving 🚗" },
                { key: "walking", label: "Walking 🚶" },
                { key: "cycling", label: "Cycling 🚴" },
              ].map((mode) => (
                <button
                  key={mode.key}
                  onClick={() => {
                    setTransportMode(
                      mode.key as
                        | "driving"
                        | "walking"
                        | "cycling"
                    );
                    setRouteGeometry([]);
                  }}
                  className={`px-3 py-2 rounded border text-sm transition-colors ${
                    transportMode === mode.key
                      ? "bg-blue-500 text-white border-blue-500"
                      : "bg-white border-gray-300 text-gray-700 hover:border-blue-400"
                  }`}
                >
                  {mode.label}
                </button>
              ))}
            </div>
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

                  {(showOptimizedRoute ? optimizedStopsState : stopsForSelectedDay).map((stop: any, idx: number) => (
                    <div
                      key={`${stop.activityId}-${idx}`}
                      className="flex items-start gap-3 bg-white border border-gray-200 rounded p-3"
                    >
                      <div className="w-6 h-6 rounded-full bg-blue-500 text-white text-xs flex items-center justify-center font-medium">
                        {idx + 2}
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
                        {showOptimizedRoute && (
                          <p className="text-xs text-green-600 mt-1 font-medium">
                            Optimized position #{idx + 2}
                          </p>
                        )}
                        {typeof stop.originalIndex === "number" && stop.originalIndex !== idx && (
                          <p className="text-xs text-orange-600 font-medium">
                            Moved from stop #{stop.originalIndex + 2}
                          </p>
                        )}
                      </div>
                      {(
                        currentLocation || idx > 0
                      ) && (
                        <div className="ml-3 flex items-center gap-2 text-xs text-gray-500 py-1">
                          <div className="h-5 border-l border-dashed border-gray-300" />

                          {routeLegs[idx] && (
                            <div className="ml-3 flex items-center gap-2 text-xs text-gray-500 py-1">
                              <div className="h-5 border-l border-dashed border-gray-300" />

                              <span>
                                ~{Math.round(routeLegs[idx].duration / 60)} min
                                • {(routeLegs[idx].distance / 1000).toFixed(1)} km
                              </span>
                            </div>
                          )}
                        </div>
                      )}
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
                        {routeDistanceKm.toFixed(1)} km.
                      </p>

                      <button
                        onClick={() => setShowOptimizedRoute(true)}
                        className="mt-3 px-3 py-2 bg-orange-500 hover:bg-orange-600 text-white text-sm rounded"
                      >
                        Show optimized route
                      </button>
                      {showOptimizedRoute && (
                        <button
                          onClick={() => setShowOptimizedRoute(false)}
                          className="mt-2 block text-xs text-gray-600 hover:text-gray-800"
                        >
                          Show original route
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
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
                zoomControl={false}
                preferCanvas={true}
                className="h-80 w-full"
              >
                <TileLayer
                  attribution="&copy; OpenStreetMap contributors"
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />
                <ZoomControl position="topleft" />
                {currentLocation && (
                  <Marker position={[currentLocation.latitude, currentLocation.longitude]}icon={createNumberedIcon(1, "#dc2626")}>
                    <Popup>Starting location</Popup>
                  </Marker>
                )}

                {(showOptimizedRoute
                    ? optimizedStopsState
                    : stopsForSelectedDay
                  ).map((stop: any, index: number) => (
                  <Marker
                    key={`${stop.activityId}-${index}`}
                    position={
                      offsetCoordinates(
                        stop.latitude,
                        stop.longitude,
                        index,
                        showOptimizedRoute
                          ? optimizedStopsState
                          : stopsForSelectedDay
                      ) as any
                    }
                  icon={createNumberedIcon(
                    index + 2,
                    showOptimizedRoute ? "#22c55e" : "#ef4444"
                  )}
                  >
                    <Popup>
                      <p className="font-semibold">{stop.activityName || stop.locationName}</p>
                      <p className="text-xs text-gray-500">{stop.timeslot}</p>
                    </Popup>
                  </Marker>
                ))}
                <Polyline
                  positions={routeGeometry as any}
                  pathOptions={{
                    color: showOptimizedRoute
                      ? "#16a34a"
                      : "#ef4444",

                    weight: showOptimizedRoute ? 6 : 4,

                    dashArray: showOptimizedRoute
                      ? undefined
                      : "8 8",

                    opacity: 0.9,
                  }}
                />
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
                  {routeDistanceKm.toFixed(1)} km
                </p>

                <p className="text-xs text-gray-600">
                  Total km
                </p>
                {showOptimizedRoute && (
                  <p className="text-xs text-green-600 mt-1">
                    Optimized route applied
                  </p>
                )}
                {showOptimizedRoute && (
                  <p className="text-xs text-green-600">
                    Saved{" "}
                    {(originalRouteKm - routeDistanceKm).toFixed(1)}
                    {" "}km
                  </p>
                )}
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

