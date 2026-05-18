import { Clock, TrendingDown, Calendar, AlertCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getWaitingTimeInsights } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function WaitingTimeAnalysis() {
  const { activePlan } = usePlanContext();
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadInsights() {
      if (!activePlan) {
        setInsights([]);
        return;
      }

      try {
        setLoading(true);
        setError("");
        const response = await getWaitingTimeInsights(activePlan.id);
        setInsights(response);
      } catch (fetchError) {
        setError(getApiErrorMessage(fetchError, "Unable to load waiting time insights."));
      } finally {
        setLoading(false);
      }
    }

    loadInsights();
  }, [activePlan?.id]);

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to see waiting time analysis for its activities." />;
  if (loading) return <ModuleLoading label="Loading waiting time analysis..." />;
  if (error) return <ModuleError message={error} />;
  if (!insights.length) return <ModuleEmpty title="No waiting-time data" description="Waiting-time insights are not available for this plan yet." />;

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <h1 className="text-2xl font-medium mb-2">Waiting Time Analysis</h1>
        <p className="text-gray-600">Recommended visit windows and wait guidance for your active plan.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        {insights.map((insight) => (
          <div key={insight.activityId} className="bg-white border-2 border-gray-300 rounded-lg p-4">
            <h3 className="font-medium mb-2">{insight.activityName}</h3>
            <div className="flex items-center gap-2 mb-1">
              <Clock className="w-4 h-4 text-gray-400" />
              <span className="text-sm">Expected wait: <span className="font-medium">{insight.expectedWaitMinutes} min</span></span>
            </div>
            <div className="flex items-center gap-2">
              <TrendingDown className="w-4 h-4 text-green-500" />
              <span className="text-sm text-gray-600">{insight.locationName}</span>
            </div>
          </div>
        ))}
      </div>

      <div className="space-y-6">
        {insights.map((insight) => (
          <div key={`${insight.activityId}-detail`} className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <div className="flex justify-between items-start gap-4 mb-4">
              <div>
                <h2 className="text-xl font-medium">{insight.activityName}</h2>
                <p className="text-sm text-gray-600">{insight.locationName}</p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-600">Expected Wait</p>
                <p className="text-2xl font-medium">{insight.expectedWaitMinutes} min</p>
              </div>
            </div>

            <div className="border border-gray-300 rounded p-4 mb-4">
              <div className="flex items-center gap-3 mb-2">
                <Calendar className="w-4 h-4 text-gray-400" />
                <span className="font-medium text-sm">{insight.suggestedWindow}</span>
              </div>
              <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                <div className="h-full bg-blue-500" style={{ width: `${Math.min(insight.expectedWaitMinutes, 90) / 90 * 100}%` }} />
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-300 rounded p-4">
              <div className="flex gap-3">
                <AlertCircle className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="font-medium text-blue-900 mb-1">Advice</p>
                  <p className="text-sm text-blue-800">{insight.advice}</p>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
