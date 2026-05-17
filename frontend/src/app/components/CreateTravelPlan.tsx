import { useMemo, useState } from "react";
import { Calendar, MapPin, Plus, Save, Check } from "lucide-react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createTravelPlan, getTravelPlanDays } from "../../api/planService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleError, ModuleLoading } from "./ModuleState";

function formatDisplayDate(dateValue) {
  return new Date(dateValue).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export function CreateTravelPlan() {
  const { destinations, loading, error, refreshPlans, setActivePlanId } = usePlanContext();
  const [step, setStep] = useState("form");
  const [formData, setFormData] = useState({
    tripName: "",
    destinationId: "",
    startDate: "",
    endDate: "",
    description: "",
  });
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [createdPlan, setCreatedPlan] = useState(null);
  const [generatedItinerary, setGeneratedItinerary] = useState([]);

  const selectedDestination = useMemo(
    () => destinations.find((destination) => destination.id === Number(formData.destinationId)) || null,
    [destinations, formData.destinationId],
  );

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      setSubmitting(true);
      setSubmitError("");

      const created = await createTravelPlan({
        name: formData.tripName,
        destinationId: Number(formData.destinationId),
        startDate: formData.startDate,
        endDate: formData.endDate,
        description: formData.description,
        status: "PLANNING",
      });

      const days = await getTravelPlanDays(created.id);

      setCreatedPlan(created);
      setGeneratedItinerary(days);
      setActivePlanId(created.id);
      setStep("generated");
      await refreshPlans();
    } catch (errorResponse) {
      setSubmitError(getApiErrorMessage(errorResponse, "Unable to create the travel plan right now."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
        <h1 className="text-2xl font-medium mb-6">Create Travel Plan</h1>

        {loading ? <ModuleLoading label="Loading destinations..." /> : null}
        {!loading && error ? <ModuleError message={error} /> : null}
        {submitError ? <ModuleError message={submitError} /> : null}

        {!loading && !error && step === "form" ? (
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Trip Name */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Trip Name *
              </label>
              <input
                type="text"
                required
                value={formData.tripName}
                onChange={e => setFormData({...formData, tripName: e.target.value})}
                placeholder="e.g., Tokyo Adventure"
                className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* Destination */}
            <div>
              <label className="block text-sm font-medium mb-2">
                Destination *
              </label>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <select
                  required
                  value={formData.destinationId}
                  onChange={(e) => setFormData({ ...formData, destinationId: e.target.value })}
                  className="w-full pl-11 pr-4 py-2 border border-gray-300 rounded bg-white focus:outline-none focus:border-blue-500"
                >
                  <option value="">Select a destination</option>
                  {destinations.map((destination) => (
                    <option key={destination.id} value={destination.id}>
                      {destination.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Date Range */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">
                  Start Date *
                </label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="date"
                    required
                    value={formData.startDate}
                    onChange={e => setFormData({...formData, startDate: e.target.value})}
                    className="w-full pl-11 pr-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  End Date *
                </label>
                <div className="relative">
                  <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="date"
                    required
                    value={formData.endDate}
                    onChange={e => setFormData({...formData, endDate: e.target.value})}
                    className="w-full pl-11 pr-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                  />
                </div>
              </div>
            </div>

            {/* Additional Options */}
            <div className="border border-gray-300 rounded p-4 bg-gray-50">
              <h3 className="font-medium mb-3">Optional Settings</h3>
              <label className="block text-sm font-medium mb-2">Description</label>
              <textarea
                rows={4}
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Add an optional description for your trip"
                className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-4 border-t border-gray-300">
              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 flex items-center gap-2"
              >
                <Plus className="w-4 h-4" />
                {submitting ? "Creating..." : "Create Travel Plan"}
              </button>
              <button
                type="button"
                onClick={() => setFormData({ tripName: "", destinationId: "", startDate: "", endDate: "", description: "" })}
                className="px-6 py-2 border border-gray-300 rounded hover:bg-gray-50"
              >
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <div className="space-y-6">
            {/* Success Message */}
            <div className="bg-green-50 border border-green-300 rounded p-4 flex items-start gap-3">
              <Check className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
              <div>
                <h3 className="font-medium text-green-900">Travel Plan Created Successfully!</h3>
                <p className="text-sm text-green-700 mt-1">
                  Your trip is ready, and the daily itinerary has been prepared for you to start organizing activities.
                </p>
              </div>
            </div>

            {/* Plan Summary */}
            <div className="border border-gray-300 rounded p-4 bg-gray-50">
              <h3 className="font-medium mb-3">Plan Summary</h3>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span className="text-gray-600">Trip Name:</span>
                  <p className="font-medium">{createdPlan?.name}</p>
                </div>
                <div>
                  <span className="text-gray-600">Destination:</span>
                  <p className="font-medium">{createdPlan?.destinationName || selectedDestination?.name}</p>
                </div>
                <div>
                  <span className="text-gray-600">Start Date:</span>
                  <p className="font-medium">{createdPlan?.startDate ? formatDisplayDate(createdPlan.startDate) : ""}</p>
                </div>
                <div>
                  <span className="text-gray-600">End Date:</span>
                  <p className="font-medium">{createdPlan?.endDate ? formatDisplayDate(createdPlan.endDate) : ""}</p>
                </div>
              </div>
            </div>

            {/* Generated Itinerary */}
            <div>
              <h3 className="font-medium mb-3">Auto-Generated Daily Structure</h3>
              <div className="space-y-3">
                {generatedItinerary.map(day => (
                  <div key={day.id} className="border border-gray-300 rounded">
                    <div className="bg-gray-100 border-b border-gray-300 px-4 py-2">
                      <h4 className="font-medium">Day {generatedItinerary.indexOf(day) + 1} - {formatDisplayDate(day.date)}</h4>
                    </div>
                    <div className="p-4">
                      <div className="space-y-2">
                        <div className="border-l-4 border-yellow-400 pl-3 py-2 bg-yellow-50">
                          <p className="text-sm font-medium">Morning (8:00 - 12:00)</p>
                          <p className="text-xs text-gray-500">
                            {day.activities?.filter((activity) => activity.timeslot === "MORNING").length || 0} activities scheduled
                          </p>
                        </div>
                        <div className="border-l-4 border-blue-400 pl-3 py-2 bg-blue-50">
                          <p className="text-sm font-medium">Afternoon (12:00 - 18:00)</p>
                          <p className="text-xs text-gray-500">
                            {day.activities?.filter((activity) => activity.timeslot === "AFTERNOON").length || 0} activities scheduled
                          </p>
                        </div>
                        <div className="border-l-4 border-purple-400 pl-3 py-2 bg-purple-50">
                          <p className="text-sm font-medium">Evening (18:00 - 23:00)</p>
                          <p className="text-xs text-gray-500">
                            {day.activities?.filter((activity) => activity.timeslot === "EVENING").length || 0} activities scheduled
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-4 border-t border-gray-300">
              <button className="px-6 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 flex items-center gap-2">
                <Save className="w-4 h-4" />
                Plan saved
              </button>
              <button
                onClick={() => setStep("form")}
                className="px-6 py-2 border border-gray-300 rounded hover:bg-gray-50"
              >
                Create Another Plan
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
