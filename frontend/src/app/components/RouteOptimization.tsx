import { Navigation, MapPin, Clock, TrendingDown, RefreshCw } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getLocationsByDestination } from "../../api/locationService";
import { getRouteOptimization } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import {MapContainer, TileLayer, Marker, Popup} from "react-leaflet";
import Routing from "./Routing";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",

  iconUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",

  shadowUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});
const redIcon = new L.Icon({
  iconUrl:
    "https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png",

  shadowUrl:
    "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",

  iconSize: [25, 41],
  iconAnchor: [12, 41],
});
export function RouteOptimization() {
  const { activePlan } = usePlanContext();
  const [locations, setLocations] = useState([]);
  const [routeData, setRouteData] = useState(null);
  const [currentLocation, setCurrentLocation] = useState<{
    latitude: number;
    longitude: number;
  } | null>(null);
  const [startLocation, setStartLocation] = useState("");
  const [suggestions, setSuggestions] = useState<any[]>([]);
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
        activePlan.destinationId
          ? getLocationsByDestination(activePlan.destinationId)
          : Promise.resolve([]),

        getRouteOptimization(activePlan.id),
      ]);

      setLocations(nextLocations);
      setRouteData(nextRoute);

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

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
          <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
            <MapPin className="w-5 h-5" />
            Destination Locations
          </h2>
          <div className="relative mb-4">
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Enter starting location"
                value={startLocation}
                onChange={(e) =>
                  setStartLocation(e.target.value)
                }
                className="flex-1 px-3 py-2 border border-gray-300 rounded"
              />

              <button
                onClick={handleSetStartLocation}
                className="px-4 py-2 bg-blue-500 text-white rounded"
              >
                Set Start
              </button>
            </div>

            {suggestions.length > 0 && (
              <div className="absolute z-50 w-full bg-white border border-gray-300 rounded mt-1 shadow-lg max-h-60 overflow-auto">

                {suggestions.map((suggestion, index) => (

                  <button
                    key={index}

                    onClick={() => {

                      setStartLocation(
                        suggestion.display_name
                      );

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

            <div className="border-2 border-gray-300 rounded overflow-hidden">
              <MapContainer
                center={
                  currentLocation
                    ? [
                        currentLocation.latitude,
                        currentLocation.longitude,
                      ]
                    : [48.8584, 2.2945]
                }
                zoom={12}
                className="h-80 w-full"
              >
                <TileLayer
                  attribution="&copy; OpenStreetMap contributors"
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                {currentLocation && (
                  <Marker
                    icon={redIcon}
                    position={[
                      currentLocation.latitude,
                      currentLocation.longitude,
                    ]}
                  >
                    <Popup>
                      Your current location
                    </Popup>
                  </Marker>
                )}

                {routeData?.stops?.map((stop: any, index: number) => (
                  <Marker
                    key={`${stop.locationId}-${index}`}
                    position={[
                      stop.latitude,
                      stop.longitude,
                    ]}
                  >
                    <Popup>
                      <div>
                        <p className="font-semibold">
                          {stop.locationName}
                        </p>

                        <p>
                          {stop.activityName}
                        </p>
                      </div>
                    </Popup>
                  </Marker>
                ))}

                {currentLocation && (
                  <Routing
                    stops={[
                      currentLocation,
                      ...(routeData?.stops || []),
                    ]}
                  />
                )}
              </MapContainer>
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
  );
}
