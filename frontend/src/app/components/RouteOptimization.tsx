import { MapPin, AlertTriangle, Calendar } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getRouteOptimization, getRouteGeometry} from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import {MapContainer, TileLayer, Marker, Popup, Polyline, ZoomControl} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";

function createNumberedIcon(number: number, color: string){
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

function offsetCoordinates(lat: number, lng: number, index: number, stops: any[]) {
  const duplicatesBefore = stops.slice(0, index).filter(
    (s: any) =>
      s.latitude === stops[index].latitude &&
      s.longitude === stops[index].longitude
  ).length;
  const offset = duplicatesBefore * 0.005;
  return [lat + offset, lng + offset,];
}

export function RouteOptimization() {
  const { activePlan } = usePlanContext();
  const [routeData, setRouteData] = useState<any>(null);
  const [currentLocation, setCurrentLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);
  const [startLocation, setStartLocation] = useState("");
  const [dayStartLocations, setDayStartLocations] = useState<
    Record<
      string,
      {
        name: string;
        latitude: number;
        longitude: number;
      }
    >
  >({});
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [selectedDay, setSelectedDay] = useState<string | null>(null);
  const [transportMode, setTransportMode] = useState<"driving" | "walking" | "cycling">("driving");
  const [routeGeometry, setRouteGeometry] = useState<any[]>([]);
  const [routeLegs, setRouteLegs] = useState<any[]>([]);
  const [routeDistanceKm, setRouteDistanceKm] = useState(0);
  const [originalRouteKm, setOriginalRouteKm] = useState(0);
  const [showOptimizedRoute, setShowOptimizedRoute] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(true);
  const [locationsLoaded, setLocationsLoaded] = useState(false);
  const allDays = routeData?.originalStops
    ? [
        ...new Set(
          routeData.originalStops.map(
            (s: any) => s.dayDate || "Date pending"
          )
        ),
      ].sort(
        (a: string, b: string) =>
          new Date(a).getTime() -
          new Date(b).getTime()
      )
    : []; 

    const stopsForSelectedDay =
      routeData?.originalStops?.filter(
        (s: any) =>
          (s.dayDate || "Date pending") === selectedDay
      ) ?? [];
    const optimizedStopsForSelectedDay =
      routeData?.optimizedStops?.filter(
        (s: any) =>
          (s.dayDate || "Date pending") === selectedDay
      ) ?? [];
    const loadRouteData = async () => {
      if (!activePlan) {
        setRouteData(null);
        setSelectedDay(null);
        return;
      }
      setCurrentLocation(null);
      setStartLocation("");
      setShowOptimizedRoute(false);
      setRouteGeometry([]);
      setRouteDistanceKm(0);
      setOriginalRouteKm(0);
      setRouteLegs([]);
      try {
        setLoading(true);
        setError("");

      const nextRoute = await getRouteOptimization(activePlan.id, null, null);
      setRouteData(nextRoute);
      setCurrentLocation(null);
      setStartLocation("");
        if (nextRoute?.originalStops?.length) {
          const sortedDates = [
            ...new Set(
              nextRoute.originalStops
                .map((s: any) => s.dayDate)
                .filter(Boolean)
            ),
          ].sort(
            (a: any, b: any) =>
              new Date(a).getTime() -
              new Date(b).getTime()
          );

          const savedDay = localStorage.getItem(
            `selected-day-${activePlan.id}`
          );

          const firstDay = String(sortedDates[0]);

          const validDay =
            savedDay &&
            sortedDates.includes(savedDay)
              ? savedDay
              : firstDay;

          setSelectedDay(validDay);
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
        if (!activePlan || !selectedDay) return;

        localStorage.setItem(
          `selected-day-${activePlan.id}`,
          selectedDay
        );
      }, [selectedDay, activePlan?.id]);

    useEffect(() => {
      if (!activePlan) return;

      const saved = localStorage.getItem(
        `route-starts-${activePlan.id}`
      );

      if (saved) {
        setDayStartLocations(JSON.parse(saved));
      } else {
        setDayStartLocations({});
      }

      setLocationsLoaded(true);
    }, [activePlan?.id]);
    
    useEffect(() => {
      if (!activePlan || !locationsLoaded) return;

      localStorage.setItem(
        `route-starts-${activePlan.id}`,
        JSON.stringify(dayStartLocations)
      );
    }, [dayStartLocations, activePlan?.id, locationsLoaded]);

    useEffect(() => {
      if (!selectedDay) return;

      setShowOptimizedRoute(false);

      const savedLocation =
        dayStartLocations[selectedDay];

      if (savedLocation) {
        setStartLocation(savedLocation.name);

        setCurrentLocation({
          latitude: savedLocation.latitude,
          longitude: savedLocation.longitude,
        });
      } else {
        setStartLocation("");
        setCurrentLocation(null);
        setRouteGeometry([]);
        setRouteLegs([]);
        setRouteDistanceKm(0);
      }
    }, [selectedDay, dayStartLocations]);

  useEffect(() => {
    const fetchRoute = async () => {

      if (!currentLocation) {
        setRouteGeometry([]);
        setRouteDistanceKm(0);
        setRouteLegs([]);
        return;
      }

      const routeStops = showOptimizedRoute
        ? optimizedStopsForSelectedDay
        : stopsForSelectedDay;

      const points = [
        [currentLocation.longitude, currentLocation.latitude],
        ...routeStops.map((stop: any) => [
          stop.longitude,
          stop.latitude,
        ]),
      ];

      if (points.length < 2) return;

      try {

        const data = await getRouteGeometry(
          points,
          transportMode
        );

        const geometry =
          data.features?.[0]?.geometry?.coordinates?.map(
            (coord: number[]) => [
              coord[1],
              coord[0],
            ]
          ) || [];

        setRouteGeometry(geometry);

        const summary =
          data.features?.[0]?.properties?.summary;

        const segments =
          data.features?.[0]?.properties?.segments || [];

        setRouteLegs(segments);

        setRouteDistanceKm(
          (summary?.distance || 0) / 1000
        );

        if (!showOptimizedRoute) {
          setOriginalRouteKm(
            (summary?.distance || 0) / 1000
          );
        }

      } catch (err) {
        console.error(err);
      }
    };

    fetchRoute();

  }, [
    currentLocation,
    selectedDay,
    transportMode,
    showOptimizedRoute,
    routeData,
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
  const routeCalculated = !!currentLocation && routeDistanceKm > 0;
  const routeWarning = routeCalculated && routeDistanceKm > 25 && stopsForSelectedDay.length >= 4;
  const routeStatusVisible = currentLocation && routeDistanceKm > 0;
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
                    {day !== "Date pending"
                      ? new Date(day).toLocaleDateString()
                      : day}
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
              {!currentLocation && (
                <div className="mb-2 px-2 py-1 bg-amber-50 border border-amber-200 rounded text-xs text-amber-700">
                  Enter a starting location to generate a route.
                </div>
              )}
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="e.g. Hotel Roma, Via Veneto..."
                  value={startLocation}
                  onChange={(e) => {setStartLocation(e.target.value); setShowSuggestions(true);}}
                  className="flex-1 px-3 py-2 border border-gray-300 rounded text-sm"
                />

                <button
                  type="button"
                  onClick={() => {
                    if (selectedDay) {
                      setDayStartLocations((prev) => {
                        const updated = { ...prev };
                        delete updated[selectedDay];
                        return updated;
                      });
                    }

                    setStartLocation("");
                    setCurrentLocation(null);
                    setShowOptimizedRoute(false);
                    setRouteGeometry([]);
                    setRouteLegs([]);
                    setRouteDistanceKm(0);
                    setSuggestions([]);
                  }}
                  className="px-3 py-2 border border-blue-500 bg-blue-500 text-white rounded text-sm hover:bg-blue-600"
                >
                  Clear
                </button>
              </div>

              {showSuggestions && suggestions.length > 0 && (
                <div className="absolute z-50 w-full bg-white border border-gray-300 rounded mt-1 shadow-lg max-h-60 overflow-auto">
                  {suggestions.map((suggestion, index) => (
                    <button
                      key={index}
                      onClick={async () => {
                        const location = {
                          name: suggestion.display_name,
                          latitude: parseFloat(suggestion.lat),
                          longitude: parseFloat(suggestion.lon),
                        };

                        if (selectedDay) {
                          setDayStartLocations((prev) => ({
                            ...prev,
                            [selectedDay]: location,
                          }));
                        }

                        setStartLocation(location.name);

                        setCurrentLocation({
                          latitude: location.latitude,
                          longitude: location.longitude,
                        });
                        if (activePlan) {
                          const optimizedRoute =
                            await getRouteOptimization(
                              activePlan.id,
                              location.latitude,
                              location.longitude
                            );

                          
                            setShowOptimizedRoute(false);

                            setRouteData(optimizedRoute);

                            setRouteGeometry([]);
                            setRouteLegs([]);
                            setRouteDistanceKm(0);
                        }
                        setShowSuggestions(false);
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
                    {routeStatusVisible && (
                      <span
                        className={`text-xs font-medium px-2 py-1 rounded-full ${
                          routeWarning
                            ? "bg-orange-100 text-orange-700"
                            : "bg-green-100 text-green-700"
                        }`}
                      >
                        {routeWarning ? "Needs optimization" : "Efficient"}
                      </span>
                    )}
                </div>

                <div className="space-y-2">

                  {currentLocation && (
                    <div className="flex items-start gap-3 bg-purple-50 border border-purple-200 rounded p-3">
                      <div className="w-6 h-6 rounded-full bg-purple-500 text-white text-xs flex items-center justify-center font-medium">
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

                  {(showOptimizedRoute ? optimizedStopsForSelectedDay : stopsForSelectedDay).map((stop: any, idx: number) => (
                    <div
                      key={`${stop.activityId}-${idx}`}
                      className="flex items-start gap-3 bg-white border border-gray-200 rounded p-3"
                    >
                      <div className="w-6 h-6 rounded-full bg-blue-500 text-white text-xs flex items-center justify-center font-medium">
                        {currentLocation ? idx + 2 : idx + 1}
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
                      {currentLocation && (
                        <div className="ml-3 flex items-center gap-2 text-xs text-gray-500 py-1">
                          <div className="h-5 border-l border-dashed border-gray-300" />

                          {routeLegs[idx] && (
                            <span>
                              ~{Math.round(routeLegs[idx].duration / 60)} min
                              {" • "}
                              {(routeLegs[idx].distance / 1000).toFixed(1)} km
                            </span>
                          )}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              {currentLocation && routeWarning && (
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
                <span className="text-sm font-normal text-gray-500 ml-1">
                  — {selectedDay}
                </span>
              )}
            </h2>
            <div className="border-2 border-gray-300 rounded overflow-hidden">
              <MapContainer
                key={`${activePlan?.id}-${selectedDay}`}
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
                  <Marker position={[currentLocation.latitude, currentLocation.longitude]}icon={createNumberedIcon(1, "#7c3aed")}>
                    <Popup>Starting location</Popup>
                  </Marker>
                )}

                {(showOptimizedRoute ? optimizedStopsForSelectedDay : stopsForSelectedDay).map((stop: any, index: number) => (
                  <Marker
                    key={`${stop.activityId}-${index}`}
                    position={
                      offsetCoordinates(
                        stop.latitude,
                        stop.longitude,
                        index,
                        showOptimizedRoute ? optimizedStopsForSelectedDay : stopsForSelectedDay
                      ) as any
                    }
                  icon={createNumberedIcon(
                    currentLocation
                      ? index + 2
                      : index + 1,
                    showOptimizedRoute ? "#22c55e" : "#ef4444"
                  )}
                  >
                    <Popup>
                      <p className="font-semibold">{stop.activityName || stop.locationName}</p>
                      <p className="text-xs text-gray-500">{stop.timeslot}</p>
                    </Popup>
                  </Marker>
                ))}
                {currentLocation && routeGeometry.length > 0 && (
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
                )}
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
                  {currentLocation
                    ? `${routeDistanceKm.toFixed(1)} km`
                    : "-"}
                </p>

                <p className="text-xs text-gray-600">
                  Total km
                </p>
              </div>

              {currentLocation ? (
                <div
                  className={`border rounded p-3 text-center ${
                    showOptimizedRoute
                      ? "border-green-300 bg-green-50"
                      : routeWarning
                      ? "border-orange-300 bg-orange-50"
                      : "border-green-300 bg-green-50"
                  }`}
                >
                  {showOptimizedRoute ? (
                    <>
                      <p className="text-xs text-green-700 font-medium">
                        Optimized route applied
                      </p>

                      <p className="text-xs text-green-600 mt-2">
                        Saved{" "}
                        {(originalRouteKm - routeDistanceKm).toFixed(1)}
                        {" "}km
                      </p>
                    </>
                  ) : (
                    <>
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
                        {routeWarning
                          ? "Needs optimization"
                          : "Efficient"}
                      </p>
                    </>
                  )}
                </div>
              ) : (
                <div className="border border-gray-300 rounded p-3 text-center">
                  <p className="text-2xl text-gray-400">-</p>

                  <p className="text-xs text-gray-500">
                    Not calculated
                  </p>
                </div>
              )}

            </div>
          </div>
        </div>

      </div>
    </div>
  );
}