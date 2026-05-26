import { MapPin, Star, Clock, Tag, Filter, Heart } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import {
  getAttractions,
  getSavedAttractions,
  saveAttraction,
  unsaveAttraction,
  addAttractionToItinerary,
  getPlanDays,
} from "../../api/planService";
export function AttractionRecommendations() {
  const { activePlan } = usePlanContext();
  const [selectedCategories, setSelectedCategories] = useState([]);
  const [attractions, setAttractions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [days, setDays] = useState<any[]>([]);
  const [selectedItineraryAttraction, setSelectedItineraryAttraction] = useState<any | null>(null);
  const [selectedDayId, setSelectedDayId] = useState<number | "">("");
  const [addedAttractionIds, setAddedAttractionIds] = useState<number[]>([]);
  const [selectedAttraction, setSelectedAttraction] = useState<any | null>(null);
  const [savedAttractionIds, setSavedAttractionIds] = useState<number[]>([]);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const categories = [
    { id: "culture", label: "Culture & History", icon: "🏛️" },
    { id: "nature", label: "Nature & Parks", icon: "🌳" },
    { id: "entertainment", label: "Entertainment", icon: "🎭" },
    { id: "food", label: "Food & Dining", icon: "🍜" },
    { id: "shopping", label: "Shopping", icon: "🛍️" },
    { id: "nightlife", label: "Nightlife", icon: "🌃" },
  ];

  useEffect(() => {
    const loadDays = async () => {
      if (!activePlan?.id) return;

      try {
        const response = await getPlanDays(activePlan.id);
        console.log("Loaded plan days:", response);
        setDays(response);
      } catch (error) {
        console.error("Unable to load plan days:", error);
        setErrorMessage("Unable to load plan days.");
      }
    };

    loadDays();
  }, [activePlan?.id]);

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

    console.log("Loaded attractions:", response);

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
  const handleViewDetails = (attraction: any) => {
  setSelectedAttraction(attraction);
  setErrorMessage("");
};

  const handleSaveAttraction = async (attraction: any) => {
  console.log("SAVE CLICKED", attraction);
  console.log("ACTIVE PLAN", activePlan);
  console.log("LOCATION ID", getAttractionId(attraction));

  if (!activePlan?.id) {
    setErrorMessage("No active plan selected.");
    return;
  }

  const locationId = getAttractionId(attraction);

  if (!locationId) {
    setErrorMessage("Unable to save attraction because location ID is missing.");
    return;
  }

  try {
    setErrorMessage("");

    const alreadySaved = savedAttractionIds.includes(locationId);

    if (alreadySaved) {
      await unsaveAttraction(activePlan.id, locationId);

      setSavedAttractionIds((previousIds) =>
        previousIds.filter((id) => id !== locationId)
      );

      setSuccessMessage(`${attraction.name} removed from saved attractions.`);
    } else {
      await saveAttraction(activePlan.id, locationId);

      setSavedAttractionIds((previousIds) => [...previousIds, locationId]);

      setSuccessMessage(`${attraction.name} saved successfully.`);
    }
  } catch (error) {
    console.error("Save attraction error:", error);
    setErrorMessage("Unable to update saved attraction.");
  }
};

const handleAddToItinerary = (attractionName: string) => {
    setSuccessMessage(`${attractionName} added to itinerary.`);
    setErrorMessage("");
  };
  const getAttractionId = (attraction: any) => {
    return attraction.locationId;
  };

  const handleConfirmAddToItinerary = async () => {
  if (!activePlan?.id) {
    setErrorMessage("No active plan selected.");
    return;
  }

  if (!selectedItineraryAttraction) {
    setErrorMessage("No attraction selected.");
    return;
  }

  if (!selectedDayId) {
    setErrorMessage("Please select a day.");
    return;
  }

  const locationId = getAttractionId(selectedItineraryAttraction);

  if (!locationId) {
    setErrorMessage("Unable to add attraction because location ID is missing.");
    return;
  }

  try {
    setErrorMessage("");

    await addAttractionToItinerary(
      activePlan.id,
      locationId,
      Number(selectedDayId)
    );

    setAddedAttractionIds((previousIds) => [...previousIds, locationId]);

    setSuccessMessage(`${selectedItineraryAttraction.name} added to itinerary.`);
    setSelectedItineraryAttraction(null);
    setSelectedDayId("");
  } catch (error) {
    console.error("Add to itinerary error:", error);
    setErrorMessage("Unable to add attraction to itinerary.");
  }
};


  return (
    <div className="max-w-7xl mx-auto">
              {successMessage && (
          <div className="mb-4 rounded border border-green-300 bg-green-50 px-4 py-3 text-green-700">
            {successMessage}
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 rounded border border-red-300 bg-red-50 px-4 py-3 text-red-700">
            {errorMessage}
          </div>
        )}
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
          <div key={getAttractionId(attraction)} className="bg-white border-2 border-gray-300 rounded-lg overflow-hidden hover:border-gray-400 transition-colors">
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
                    <button
                      onClick={() => {
                        setSelectedItineraryAttraction(attraction);
                        setSelectedDayId("");
                      }}
                      disabled={addedAttractionIds.includes(getAttractionId(attraction))}
                      className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
                    >
                      {addedAttractionIds.includes(getAttractionId(attraction))
                        ? "Added"
                        : "Add to Itinerary"}
                    </button>
                    <button
                      onClick={() => handleViewDetails(attraction)}
                      className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
                    >
                      View Details
                    </button>
                    <button
                      onClick={() => handleSaveAttraction(attraction)}
                      className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 flex items-center gap-2"
                    >
                      <Heart className="w-4 h-4" />
                      {savedAttractionIds.includes(getAttractionId(attraction)) ? "Saved" : "Save"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
      {selectedAttraction && (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
    <div className="w-full max-w-lg rounded-lg border border-gray-300 bg-white p-6 shadow-lg">
      <div className="mb-4 flex items-start justify-between gap-4">
        <div>
          <h2 className="text-xl font-semibold">{selectedAttraction.name}</h2>
          <p className="text-sm text-gray-600">
            {selectedAttraction.category || selectedAttraction.categoryName || "Attraction"}
          </p>
        </div>

        <button
          onClick={() => setSelectedAttraction(null)}
          className="text-gray-500 hover:text-gray-800"
        >
          ✕
        </button>
      </div>

      <div className="space-y-3 text-sm text-gray-700">
        <p>
          <span className="font-medium">Description: </span>
          {selectedAttraction.description || "No description available."}
        </p>

        <p>
          <span className="font-medium">Location: </span>
          {selectedAttraction.address ||
            selectedAttraction.locationName ||
            selectedAttraction.location ||
            "Location not available."}
        </p>

        <p>
          <span className="font-medium">Destination: </span>
          {selectedAttraction.destinationName ||
            selectedAttraction.destination ||
            "Destination not available."}
        </p>

        <p>
          <span className="font-medium">Status: </span>
          {selectedAttraction.status || "RECOMMENDED"}
        </p>
      </div>

      <div className="mt-6 flex justify-end gap-3">
        <button
          onClick={() => setSelectedAttraction(null)}
          className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
        >
          Close
        </button>

        <button
          onClick={() => {
            handleAddToItinerary(selectedAttraction.name);
            setSelectedAttraction(null);
          }}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          Add to Itinerary
        </button>
      </div>
    </div>
  </div>
)}
    {selectedItineraryAttraction && (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
    <div className="w-full max-w-md rounded-lg border border-gray-300 bg-white p-6 shadow-lg">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">Add to Itinerary</h2>
        <p className="mt-1 text-sm text-gray-600">
          Choose a day for {selectedItineraryAttraction.name}.
        </p>
      </div>

      <label className="block text-sm font-medium text-gray-700 mb-2">
        Day
      </label>

      <select
        value={selectedDayId}
        onChange={(event) => setSelectedDayId(Number(event.target.value))}
        className="w-full border border-gray-300 rounded px-3 py-2 mb-4"
      >
        <option value="">Select day</option>

        {days.map((day, index) => (
          <option key={day.id} value={day.id}>
            {day.date
              ? `Day ${index + 1} - ${day.date}`
              : `Day ${index + 1}`}
          </option>
        ))}
      </select>

      {days.length === 0 && (
        <p className="mb-4 text-sm text-red-600">
          No days found for this plan.
        </p>
      )}

      <div className="flex justify-end gap-3">
        <button
          onClick={() => {
            setSelectedItineraryAttraction(null);
            setSelectedDayId("");
          }}
          className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
        >
          Cancel
        </button>

        <button
          onClick={handleConfirmAddToItinerary}
          disabled={!selectedDayId || days.length === 0}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          Add
        </button>
      </div>
    </div>
  </div>
)}
    </div>
  );
}
