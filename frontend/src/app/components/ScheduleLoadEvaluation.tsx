import { Activity, AlertTriangle, CheckCircle, TrendingUp, Clock } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getScheduleLoad } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function ScheduleLoadEvaluation() {
  const { activePlan } = usePlanContext();
  const [scheduleLoad, setScheduleLoad] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadSchedule() {
      if (!activePlan) {
        setScheduleLoad(null);
        return;
      }

      try {
        setLoading(true);
        setError("");
        const response = await getScheduleLoad(activePlan.id);
        setScheduleLoad(response);
      } catch (fetchError) {
        setError(getApiErrorMessage(fetchError, "Unable to evaluate schedule load."));
      } finally {
        setLoading(false);
      }
    }

    loadSchedule();
  }, [activePlan?.id]);

  const getIntensityColor = (intensity) => {
    switch ((intensity || "").toLowerCase()) {
      case "light":
        return "bg-green-100 border-green-400 text-green-700";
      case "moderate":
        return "bg-blue-100 border-blue-400 text-blue-700";
      case "busy":
        return "bg-yellow-100 border-yellow-400 text-yellow-700";
      default:
        return "bg-red-100 border-red-400 text-red-700";
    }
  };

  const getIntensityIcon = (intensity) => {
    switch ((intensity || "").toLowerCase()) {
      case "light":
        return <CheckCircle className="w-5 h-5 text-green-600" />;
      case "moderate":
        return <Activity className="w-5 h-5 text-blue-600" />;
      default:
        return <AlertTriangle className="w-5 h-5 text-yellow-600" />;
    }
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to analyze its daily workload." />;
  if (loading) return <ModuleLoading label="Analyzing schedule load..." />;
  if (error) return <ModuleError message={error} />;
  if (!scheduleLoad?.dailyLoads?.length) {
    return <ModuleEmpty title="No schedule load data" description="Add activities to your plan first so daily intensity can be evaluated." />;
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <h1 className="text-2xl font-medium mb-2">Schedule Load Evaluation</h1>
        <p className="text-gray-600">{scheduleLoad.summary}</p>
      </div>

      <div className="space-y-6">
        {scheduleLoad.dailyLoads.map((day, index) => (
          <div key={day.dayId} className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <div className="flex justify-between items-start gap-4 mb-4">
              <div>
                <h2 className="text-xl font-medium">Day {index + 1}</h2>
                <p className="text-sm text-gray-600">{new Date(day.date).toLocaleDateString()}</p>
              </div>
              <span className={`px-3 py-1 text-sm border rounded ${getIntensityColor(day.intensity)}`}>
                {day.intensity}
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              <div className="border border-gray-300 rounded p-4">
                <p className="text-sm text-gray-600 mb-1">Activities</p>
                <p className="text-2xl font-medium">{day.activityCount}</p>
              </div>
              <div className="border border-gray-300 rounded p-4">
                <p className="text-sm text-gray-600 mb-1">Duration</p>
                <p className="text-2xl font-medium">{Math.round((day.totalDurationMinutes || 0) / 60)}h</p>
              </div>
              <div className="border border-gray-300 rounded p-4 flex items-center gap-3">
                {getIntensityIcon(day.intensity)}
                <div>
                  <p className="text-sm text-gray-600">Intensity</p>
                  <p className="font-medium">{day.intensity}</p>
                </div>
              </div>
            </div>

            {day.warning ? (
              <div className="bg-yellow-50 border border-yellow-300 rounded p-4 text-sm text-yellow-800">
                {day.warning}
              </div>
            ) : null}
          </div>
        ))}
      </div>
    </div>
  );
}
