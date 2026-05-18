import { Utensils, MapPin, Calendar, Star, Clock, Filter } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getLocalRecommendations } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function LocalRecommendations() {
  const { activePlan } = usePlanContext();
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("restaurants");

  useEffect(() => {
    async function loadRecommendations() {
      if (!activePlan) {
        setRecommendations([]);
        return;
      }

      try {
        setLoading(true);
        setError("");
        const response = await getLocalRecommendations(activePlan.id);
        setRecommendations(response);
      } catch (fetchError) {
        setError(getApiErrorMessage(fetchError, "Unable to load local recommendations."));
      } finally {
        setLoading(false);
      }
    }

    loadRecommendations();
  }, [activePlan?.id]);

  const filteredRecommendations = useMemo(() => {
    return recommendations.filter((item) =>
      activeTab === "restaurants" ? item.type?.toLowerCase().includes("restaurant") : !item.type?.toLowerCase().includes("restaurant"),
    );
  }, [activeTab, recommendations]);

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to load nearby local recommendations." />;
  if (loading) return <ModuleLoading label="Loading local recommendations..." />;
  if (error) return <ModuleError message={error} />;

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-center gap-2 mb-4">
          <Filter className="w-5 h-5" />
          <h2 className="text-xl font-medium">Local Recommendations</h2>
        </div>
        <div className="flex gap-3">
          <button
            onClick={() => setActiveTab("restaurants")}
            className={`px-4 py-2 border rounded text-sm ${activeTab === "restaurants" ? "border-blue-500 bg-blue-50 text-blue-700" : "border-gray-300"}`}
          >
            Restaurants
          </button>
          <button
            onClick={() => setActiveTab("events")}
            className={`px-4 py-2 border rounded text-sm ${activeTab === "events" ? "border-blue-500 bg-blue-50 text-blue-700" : "border-gray-300"}`}
          >
            Events & Activities
          </button>
        </div>
      </div>

      {filteredRecommendations.length === 0 ? (
        <ModuleEmpty
          title="No local recommendations available"
          description="No nearby matches were found for this destination and the selected filter."
        />
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {filteredRecommendations.map((item) => (
            <div key={item.locationId} className="bg-white border-2 border-gray-300 rounded-lg p-6">
              <div className="flex gap-4">
                <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Utensils className="w-6 h-6 text-blue-600" />
                </div>
                <div className="flex-1">
                  <div className="flex justify-between items-start gap-4 mb-2">
                    <div>
                      <h3 className="font-medium">{item.name}</h3>
                      <p className="text-sm text-gray-600">{item.type}</p>
                    </div>
                    <div className="flex items-center gap-1 text-sm text-gray-500">
                      <Star className="w-4 h-4 text-yellow-500" />
                      <span>Local pick</span>
                    </div>
                  </div>
                  <div className="space-y-2 text-sm text-gray-600">
                    <p className="flex items-center gap-2"><MapPin className="w-4 h-4" /> {item.address || "Address unavailable"}</p>
                    <p className="flex items-center gap-2"><Clock className="w-4 h-4" /> {item.bestTimeslot || "Flexible timing"}</p>
                    <p className="flex items-center gap-2"><Calendar className="w-4 h-4" /> {item.context}</p>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
