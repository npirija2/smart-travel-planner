import { Bookmark, Hotel, Plane, MapPin, Calendar, Plus, CheckCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createReservation, getReservationsForPlan } from "../../api/reservationService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function ReservationManagement() {
  const { activePlan } = usePlanContext();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({
    type: "hotel",
    details: "",
    startDate: "",
    endDate: "",
    price: "",
    status: "CONFIRMED",
  });

  const loadReservations = async () => {
    if (!activePlan) {
      setReservations([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const response = await getReservationsForPlan(activePlan.id);
      setReservations(response);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to load reservations."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReservations();
  }, [activePlan?.id]);

  const getIcon = (type) => {
    switch ((type || "").toLowerCase()) {
      case "hotel":
        return <Hotel className="w-5 h-5" />;
      case "flight":
        return <Plane className="w-5 h-5" />;
      default:
        return <MapPin className="w-5 h-5" />;
    }
  };

  const handleCreateReservation = async (event) => {
    event.preventDefault();
    await createReservation({
      ...formData,
      planId: activePlan.id,
      price: Number(formData.price),
      startDate: new Date(formData.startDate).toISOString(),
      endDate: new Date(formData.endDate).toISOString(),
    });
    setFormData({ type: "hotel", details: "", startDate: "", endDate: "", price: "", status: "CONFIRMED" });
    await loadReservations();
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to manage its reservations." />;
  if (loading) return <ModuleLoading label="Loading reservations..." />;
  if (error) return <ModuleError message={error} />;

  return (
    <div className="max-w-7xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          {reservations.length === 0 ? (
            <ModuleEmpty title="No reservations yet" description="Create hotel, flight, or activity reservations to populate this module." />
          ) : (
            reservations.map((reservation) => (
              <div key={reservation.id} className="bg-white border-2 border-gray-300 rounded-lg p-6">
                <div className="flex justify-between items-start gap-4">
                  <div className="flex gap-3">
                    <div className="w-10 h-10 bg-gray-100 border border-gray-300 rounded flex items-center justify-center">
                      {getIcon(reservation.type)}
                    </div>
                    <div>
                      <h3 className="font-medium capitalize">{reservation.type}</h3>
                      <p className="text-sm text-gray-600">{reservation.details || "No details provided"}</p>
                      <p className="text-sm text-gray-500">
                        {new Date(reservation.startDate).toLocaleString()} - {new Date(reservation.endDate).toLocaleString()}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="inline-flex items-center gap-1 px-3 py-1 text-sm bg-green-100 border border-green-400 text-green-700 rounded">
                      <CheckCircle className="w-4 h-4" />
                      {reservation.status}
                    </div>
                    <p className="font-medium mt-2">{reservation.price?.toFixed(2)}</p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        <form onSubmit={handleCreateReservation} className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3 h-fit">
          <h3 className="font-medium flex items-center gap-2">
            <Plus className="w-4 h-4" />
            Add Reservation
          </h3>
          <select
            value={formData.type}
            onChange={(event) => setFormData({ ...formData, type: event.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded"
          >
            <option value="hotel">Hotel</option>
            <option value="flight">Flight</option>
            <option value="activity">Activity</option>
          </select>
          <textarea
            required
            rows={3}
            value={formData.details}
            onChange={(event) => setFormData({ ...formData, details: event.target.value })}
            placeholder="Reservation details"
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
          <input type="datetime-local" required value={formData.startDate} onChange={(event) => setFormData({ ...formData, startDate: event.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded" />
          <input type="datetime-local" required value={formData.endDate} onChange={(event) => setFormData({ ...formData, endDate: event.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded" />
          <input type="number" required min="0" step="0.01" value={formData.price} onChange={(event) => setFormData({ ...formData, price: event.target.value })} placeholder="Price" className="w-full px-3 py-2 border border-gray-300 rounded" />
          <button className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600">
            Save Reservation
          </button>
        </form>
      </div>
    </div>
  );
}
