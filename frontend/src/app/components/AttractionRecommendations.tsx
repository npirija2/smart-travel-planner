import { MapPin, Star, Clock, Tag, Filter, Heart } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getAttractions } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function AttractionRecommendations() {
  const { activePlan } = usePlanContext();
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [attractions, setAttractions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const categories = [
    { id: "culture", label: "Culture & History", icon: "🏛️" },
    { id: "nature", label: "Nature & Parks", icon: "🌳" },
    { id: "entertainment", label: "Entertainment", icon: "🎭" },
    { id: "food", label: "Food & Dining", icon: "🍜" },
    { id: "shopping", label: "Shopping", icon: "🛍️" },
    { id: "nightlife", label: "Nightlife", icon: "🌃" },
  ];

  useEffect(() => {
    async function loadAttractions() {
      if (!activePlan) {
        setAttractions([]);
        return;
      }

      try {
        setLoading(true);
        setError("");
        const interest = selectedCategories.join(",");
        const response = await getAttractions(activePlan.id, interest);
        setAttractions(response);
      } catch (fetchError) {
        setError(getApiErrorMessage(fetchError, "Unable to load attraction recommendations."));
      } finally {
        setLoading(false);
      }
    }

    loadAttractions();
  }, [activePlan?.id, selectedCategories.join(",")]);

  const toggleCategory = (id) => {
    setSelectedCategories((previous) =>
      previous.includes(id) ? previous.filter((category) => category !== id) : [...previous, id],
    );
  };

  if (!activePlan) {
    return <ModuleEmpty title="No active plan selected" description="Choose a plan first to get destination-aware attraction recommendations." />;
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-center gap-2 mb-4">
          <Filter className="w-5 h-5" />
          <h2 className="text-xl font-medium">Filter by Interest</h2>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
          {categories.map((category) => (
            <button
              key={category.id}
              onClick={() => toggleCategory(category.id)}
              className={`border-2 rounded p-3 text-sm transition-colors ${
                selectedCategories.includes(category.id)
                  ? "border-blue-500 bg-blue-50 text-blue-700"
                  : "border-gray-300 hover:bg-gray-50"
              }`}
            >
              <div className="text-2xl mb-1">{category.icon}</div>
              <div className="font-medium">{category.label}</div>
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white border-2 border-gray-300 rounded-lg p-4 mb-6">
        <div className="flex justify-between items-center">
          <div>
            <h2 className="text-xl font-medium">Recommended for You</h2>
            <p className="text-sm text-gray-600 mt-1">
              Based on your active plan destination: {activePlan.destinationName || "Unknown destination"}
            </p>
          </div>
        </div>
      </div>

      {loading ? <ModuleLoading label="Loading recommendations..." /> : null}
      {!loading && error ? <ModuleError message={error} /> : null}
      {!loading && !error && attractions.length === 0 ? (
        <ModuleEmpty
          title="No recommendations available"
          description="No matching attractions were found for the current destination and selected interests."
        />
      ) : null}

      <div className="space-y-4">
        {attractions.map((attraction) => (
          <div key={attraction.locationId} className="bg-white border-2 border-gray-300 rounded-lg overflow-hidden hover:border-gray-400 transition-colors">
            <div className="p-6">
              <div className="flex gap-6">
                <div className="w-48 h-32 bg-gray-200 border border-gray-300 rounded flex-shrink-0 flex items-center justify-center">
                  <MapPin className="w-8 h-8 text-gray-400" />
                </div>

                <div className="flex-1">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h3 className="text-lg font-medium mb-1">{attraction.name}</h3>
                      <div className="flex items-center gap-3 text-sm">
                        <div className="flex items-center gap-1">
                          <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                          <span className="font-medium">{attraction.matchScore}</span>
                          <span className="text-gray-500">match score</span>
                        </div>
                        <div className="flex items-center gap-1 text-gray-600">
                          <Clock className="w-4 h-4" />
                          <span>{attraction.type || "Flexible visit"}</span>
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="bg-green-100 border border-green-400 text-green-700 px-3 py-1 rounded text-sm font-medium">
                        {attraction.matchScore}% Match
                      </div>
                    </div>
                  </div>

                  <p className="text-sm text-gray-700 mb-3">{attraction.reason}</p>

                  <div className="flex flex-wrap gap-2 mb-3">
                    {[attraction.type, attraction.destinationName].filter(Boolean).map((tag) => (
                      <span key={tag} className="px-2 py-1 bg-gray-100 border border-gray-300 rounded text-xs flex items-center gap-1">
                        <Tag className="w-3 h-3" />
                        {tag}
                      </span>
                    ))}
                  </div>

                  <p className="text-sm text-gray-600 mb-3">{attraction.address || "Address unavailable"}</p>

                  <div className="flex gap-3">
                    <button className="px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 text-sm">
                      Add to Itinerary
                    </button>
                    <button className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 text-sm">
                      View Details
                    </button>
                    <button className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 text-sm flex items-center gap-1">
                      <Heart className="w-4 h-4" />
                      Save
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
