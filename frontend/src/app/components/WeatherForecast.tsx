import { Cloud, CloudRain, Sun, Wind, Droplets, Umbrella, TrendingUp } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getWeatherForecast } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function WeatherForecast() {
  const { activePlan } = usePlanContext();
  const [forecast, setForecast] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [emptyMessage, setEmptyMessage] = useState("");

  useEffect(() => {
    async function loadForecast() {
      if (!activePlan) {
        setForecast([]);
        setEmptyMessage("");
        return;
      }

      try {
        setLoading(true);
        setError("");
        setEmptyMessage("");
        const response = await getWeatherForecast(activePlan.id);
        setForecast(response);
      } catch (fetchError) {
        if (fetchError?.response?.status === 404) {
          setForecast([]);
          setError("");
          setEmptyMessage(
            getApiErrorMessage(fetchError, "Forecast details are not available for this plan yet.")
          );
          return;
        }
        setError(getApiErrorMessage(fetchError, "Unable to load weather forecast."));
      } finally {
        setLoading(false);
      }
    }

    loadForecast();
  }, [activePlan?.id]);

  const summary = useMemo(() => forecast[0] || null, [forecast]);

  const getWeatherIcon = (condition) => {
    const normalized = condition?.toLowerCase() || "";
    if (normalized.includes("sun")) return <Sun className="w-12 h-12 text-yellow-500" />;
    if (normalized.includes("rain")) return <CloudRain className="w-12 h-12 text-blue-500" />;
    return <Cloud className="w-12 h-12 text-gray-500" />;
  };

  if (!activePlan) {
    return <ModuleEmpty title="No active plan selected" description="Choose a plan first to load its destination forecast." />;
  }

  if (loading) return <ModuleLoading label="Loading weather forecast..." />;
  if (error) return <ModuleError message={error} />;
  if (!forecast.length) {
    return (
      <ModuleEmpty
        title="No forecast available"
        description={emptyMessage || "Forecast details are not available for this plan yet."}
      />
    );
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start justify-between gap-6">
          <div>
            <h1 className="text-2xl font-medium mb-2">Weather Forecast</h1>
            <p className="text-gray-600">
              Forecast and activity guidance for {activePlan.destinationName || "your selected destination"}.
            </p>
          </div>
          <div className="flex items-center gap-4">
            {getWeatherIcon(summary.condition)}
            <div className="text-right">
              <p className="text-3xl font-medium">{summary.temperatureCelsius}°C</p>
              <p className="text-sm text-gray-600">{summary.condition}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white border-2 border-gray-300 rounded-lg p-6">
          <h2 className="text-xl font-medium mb-4">Upcoming Forecast</h2>
          <div className="space-y-3">
            {forecast.map((day) => (
              <div key={day.date} className="border border-gray-300 rounded p-4">
                <div className="flex justify-between items-center gap-4">
                  <div className="flex items-center gap-4">
                    {getWeatherIcon(day.condition)}
                    <div>
                      <h3 className="font-medium">{new Date(day.date).toLocaleDateString()}</h3>
                      <p className="text-sm text-gray-600">{day.condition}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-medium">{day.temperatureCelsius}°C</p>
                    <p className="text-xs text-gray-500">{day.suggestedTimeslot || "Flexible timeslot"}</p>
                  </div>
                </div>
                <div className="mt-3 grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
                  <div className="bg-gray-50 border border-gray-300 rounded p-3 flex items-center gap-2">
                    <Droplets className="w-4 h-4 text-blue-500" />
                    <span>{day.recommendation}</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-300 rounded p-3 flex items-center gap-2">
                    <Wind className="w-4 h-4 text-gray-500" />
                    <span>{day.suggestedTimeslot || "No preferred time"}</span>
                  </div>
                  <div className="bg-gray-50 border border-gray-300 rounded p-3 flex items-center gap-2">
                    <Umbrella className="w-4 h-4 text-gray-500" />
                    <span>{day.condition}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-4">
            <h3 className="font-medium mb-3 flex items-center gap-2">
              <TrendingUp className="w-4 h-4" />
              Activity Guidance
            </h3>
            <div className="space-y-3">
              {forecast.map((day) => (
                <div key={`${day.date}-guide`} className="border border-gray-300 rounded p-3 bg-gray-50">
                  <p className="text-sm font-medium mb-1">{new Date(day.date).toLocaleDateString()}</p>
                  <p className="text-sm text-gray-600">{day.recommendation}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
