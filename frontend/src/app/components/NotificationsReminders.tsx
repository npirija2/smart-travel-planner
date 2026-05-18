import { Bell, Calendar, CheckCircle, Clock, Settings } from "lucide-react";
import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createNotification, getNotificationsByPlan } from "../../api/notificationService";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function NotificationsReminders() {
  const { currentUser } = useAuth();
  const { activePlan } = usePlanContext();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState({ message: "", type: "REMINDER", date: "" });

  const loadNotifications = async () => {
    if (!activePlan) {
      setNotifications([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const response = await getNotificationsByPlan(activePlan.id);
      setNotifications(response);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to load notifications."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [activePlan?.id]);

  const handleCreateNotification = async (event) => {
    event.preventDefault();
    await createNotification({
      message: formData.message,
      type: formData.type,
      date: new Date(formData.date).toISOString(),
      planId: activePlan.id,
      userId: currentUser.id,
    });
    setFormData({ message: "", type: "REMINDER", date: "" });
    await loadNotifications();
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to manage reminders and notifications." />;
  if (loading) return <ModuleLoading label="Loading notifications..." />;
  if (error) return <ModuleError message={error} />;

  return (
    <div className="max-w-6xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          {notifications.length === 0 ? (
            <ModuleEmpty title="No notifications yet" description="Create reminders for your active plan to populate this module." />
          ) : (
            notifications.map((notification) => (
              <div key={notification.id} className="bg-white border-2 border-gray-300 rounded-lg p-6">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Bell className="w-6 h-6 text-blue-600" />
                  </div>
                  <div className="flex-1">
                    <div className="flex justify-between items-start gap-4 mb-2">
                      <h3 className="font-medium">{notification.type}</h3>
                      <span className="text-xs text-gray-500">{new Date(notification.date).toLocaleString()}</span>
                    </div>
                    <p className="text-sm text-gray-700">{notification.message}</p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        <form onSubmit={handleCreateNotification} className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3 h-fit">
          <h3 className="font-medium flex items-center gap-2">
            <Settings className="w-4 h-4" />
            Add Reminder
          </h3>
          <textarea
            required
            rows={3}
            value={formData.message}
            onChange={(event) => setFormData({ ...formData, message: event.target.value })}
            placeholder="Reminder message"
            className="w-full px-3 py-2 border border-gray-300 rounded"
          />
          <select value={formData.type} onChange={(event) => setFormData({ ...formData, type: event.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded">
            <option value="REMINDER">Reminder</option>
            <option value="TRANSPORT">Transport</option>
            <option value="RESERVATION">Reservation</option>
          </select>
          <input type="datetime-local" required value={formData.date} onChange={(event) => setFormData({ ...formData, date: event.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded" />
          <button className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600">
            Save Reminder
          </button>
          <div className="text-xs text-gray-500 flex items-center gap-2">
            <Calendar className="w-4 h-4" />
            Saved for this trip and ready to review anytime
          </div>
        </form>
      </div>
    </div>
  );
}
