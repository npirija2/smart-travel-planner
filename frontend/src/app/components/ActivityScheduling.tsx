import { Calendar, Clock, Plus, AlertCircle, Pencil, Trash2, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { createActivity, deleteActivity, updateActivity } from "../../api/activityService";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getTravelPlanDays } from "../../api/planService";
import { createLocation, getLocationsByDestination } from "../../api/locationService";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

const EMPTY_ACTIVITY_FORM = {
  name: "",
  description: "",
  dayId: "",
  locationId: "",
  timeslot: "MORNING",
  startTime: "09:00",
  endTime: "10:00",
  duration: "60",
  status: "PLANNED",
};

const EMPTY_LOCATION_FORM = {
  name: "",
  address: "",
  type: "ATTRACTION",
};

function getDefaultTimeslotRange(timeslot) {
  switch (timeslot) {
    case "AFTERNOON":
      return { startTime: "13:00", endTime: "14:30" };
    case "EVENING":
      return { startTime: "18:00", endTime: "19:30" };
    default:
      return { startTime: "09:00", endTime: "10:30" };
  }
}

function getDurationInMinutes(startTime, endTime) {
  if (!startTime || !endTime) {
    return 0;
  }

  const [startHour, startMinute] = startTime.split(":").map(Number);
  const [endHour, endMinute] = endTime.split(":").map(Number);

  const start = startHour * 60 + startMinute;
  const end = endHour * 60 + endMinute;

  return Math.max(end - start, 0);
}

function timeToMinutes(time) {
  const [hours, minutes] = time.split(":").map(Number);
  return hours * 60 + minutes;
}

function toApiActivityPayload(formState, createdBy) {
  return {
    name: formState.name.trim(),
    description: formState.description.trim(),
    dayId: Number(formState.dayId),
    createdBy: createdBy ? Number(createdBy) : null,
    locationId: Number(formState.locationId),
    timeslot: formState.timeslot,
    startTime: formState.startTime || null,
    endTime: formState.endTime || null,
    duration: getDurationInMinutes(formState.startTime, formState.endTime, ),
    status: formState.status || "PLANNED",
  };
}

function toFormState(activity) {
  return {
    name: activity.name || "",
    description: activity.description || "",
    dayId: String(activity.dayId || ""),
    locationId: String(activity.locationId || ""),
    timeslot: activity.timeslot || "MORNING",
    startTime: activity.startTime ? String(activity.startTime).slice(0, 5) : "",
    endTime: activity.endTime ? String(activity.endTime).slice(0, 5) : "",
    duration: activity.duration ? String(activity.duration) : "",
    status: activity.status || "PLANNED",
  };
}

export function ActivityScheduling() {
  const { activePlan } = usePlanContext();
  const { currentUser } = useAuth();
  const [days, setDays] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [editingActivityId, setEditingActivityId] = useState(null);
  const [activityForm, setActivityForm] = useState(EMPTY_ACTIVITY_FORM);
  const [locationForm, setLocationForm] = useState(EMPTY_LOCATION_FORM);
  const [locationError, setLocationError] = useState("");
  const [savingLocation, setSavingLocation] = useState(false);
  const [openFormSlot, setOpenFormSlot] = useState(null);
  const [showLocationForm, setShowLocationForm] = useState(false);

  const timeSlots = [
    { id: "MORNING", label: "Morning", time: "8:00 - 12:00", color: "yellow" },
    { id: "AFTERNOON", label: "Afternoon", time: "12:00 - 18:00", color: "blue" },
    { id: "EVENING", label: "Evening", time: "18:00 - 23:00", color: "purple" },
  ];

  const TIMESLOT_LIMITS = {
    MORNING: {
      start: "08:00",
      end: "12:00",
    },
    AFTERNOON: {
      start: "12:00",
      end: "18:00",
    },
    EVENING: {
      start: "18:00",
      end: "23:00",
    },
  };
  async function loadSchedulingData() {
    if (!activePlan) {
      setDays([]);
      setLocations([]);
      return;
    }

    try {
      setError("");
      const [dayResponse, locationResponse] = await Promise.all([
        getTravelPlanDays(activePlan.id),
        activePlan.destinationId ? getLocationsByDestination(activePlan.destinationId) : Promise.resolve([]),
      ]);

      setDays(dayResponse);
      setLocations(locationResponse);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to load activity scheduling data."));
    } finally {
    }
  }

  useEffect(() => {
    loadSchedulingData();
  }, [activePlan?.id]);

  useEffect(() => {
    if (!days.length) {
      setActivityForm(EMPTY_ACTIVITY_FORM);
      return;
    }

    setActivityForm((currentForm) => {
      const nextDayId = currentForm.dayId || String(days[0].id);
      const nextLocationId = currentForm.locationId || (locations[0] ? String(locations[0].id) : "");
      return {
        ...currentForm,
        dayId: nextDayId,
        locationId: nextLocationId,
      };
    });
  }, [days, locations]);

  const conflicts = useMemo(() => {
    return days.flatMap((day, index) =>
      timeSlots
        .map((slot) => {
          const activities = (day.activities || []).filter((activity) => activity.timeslot === slot.id);
          return activities.length > 2
            ? { day: index + 1, slot: slot.label.toLowerCase(), message: "This time slot may be too packed." }
            : null;
        })
        .filter(Boolean),
    );
  }, [days]);

  const unscheduledActivities = useMemo(
    () =>
      days
        .flatMap((day) => (day.activities || []).map((activity) => ({ ...activity, dayDate: day.date })))
        .sort((left, right) => {
          if ((left.dayDate || "") !== (right.dayDate || "")) {
            return String(left.dayDate || "").localeCompare(String(right.dayDate || ""));
          }

          return String(left.name || "").localeCompare(String(right.name || ""));
        }),
    [days],
  );

  const canSubmitActivity = Boolean(
    activityForm.name.trim() &&
    activityForm.dayId &&
    activityForm.locationId,
  );

  const resetForm = () => {
    setEditingActivityId(null);
    setSubmitError("");
    setActivityForm({
      ...EMPTY_ACTIVITY_FORM,
      dayId: days[0] ? String(days[0].id) : "",
      locationId: locations[0] ? String(locations[0].id) : "",
    });
  };

  const resetLocationForm = () => {
    setLocationForm(EMPTY_LOCATION_FORM);
    setLocationError("");
  };

  const handleTimeslotChange = (nextTimeslot) => {
    const nextTimes = getDefaultTimeslotRange(nextTimeslot);
    setActivityForm((currentForm) => ({
      ...currentForm,
      timeslot: nextTimeslot,
      startTime: currentForm.startTime || nextTimes.startTime,
      endTime: currentForm.endTime || nextTimes.endTime,
    }));
  };

  const handleEdit = (activity) => {
    setEditingActivityId(activity.id);
    setSubmitError("");
    setActivityForm(toFormState(activity));

    setOpenFormSlot({
      dayId: activity.dayId,
      timeslot: activity.timeslot,
    });
  };

  const handleOpenForm = (dayId, timeslot) => {
    const defaultTimes = getDefaultTimeslotRange(timeslot);
    setEditingActivityId(null);
    setActivityForm({
      ...EMPTY_ACTIVITY_FORM,
      dayId: String(dayId),
      locationId: locations[0] ? String(locations[0].id) : "",
      timeslot,
      startTime: defaultTimes.startTime,
      endTime: defaultTimes.endTime,
    });

    setOpenFormSlot({
      dayId,
      timeslot,
    });
  };

  const handleDelete = async (activityId) => {
    try {
      setSubmitting(true);
      setSubmitError("");
      await deleteActivity(activityId);
      await loadSchedulingData();
      if (editingActivityId === activityId) {
        resetForm();
      }
      setOpenFormSlot(null);
    } catch (deleteError) {
      setSubmitError(getApiErrorMessage(deleteError, "Unable to remove this activity right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!canSubmitActivity) {
      return;
    }
    const slotLimits = TIMESLOT_LIMITS[activityForm.timeslot];
    const startMinutes = timeToMinutes(activityForm.startTime);
    const endMinutes = timeToMinutes(activityForm.endTime);
    const slotStart = timeToMinutes(slotLimits.start);
    const slotEnd = timeToMinutes(slotLimits.end);

    if (startMinutes >= endMinutes) {
      setSubmitError("End time cannot be earlier than start time.");
      return;
    }

    if (startMinutes < slotStart || endMinutes > slotEnd) {
      setSubmitError(
        `${activityForm.timeslot.toLowerCase()} activities must stay between ${slotLimits.start} and ${slotLimits.end}.`
      );
      return;
    }
    
    const selectedDay = days.find(
      (day) => String(day.id) === activityForm.dayId
    );

    const overlappingActivity = (selectedDay?.activities || []).find(
      (activity) => {
        if (
          editingActivityId &&
          activity.id === editingActivityId
        ) {
          return false;
        }

        if (activity.timeslot !== activityForm.timeslot) {
          return false;
        }

        const existingStart = timeToMinutes(activity.startTime);
        const existingEnd = timeToMinutes(activity.endTime);

        return (
          startMinutes < existingEnd &&
          endMinutes > existingStart
        );
      }
    );

    if (overlappingActivity) {
      setSubmitError(
        `This overlaps with "${overlappingActivity.name}".`
      );
      return;
    }

    try {
      setSubmitting(true);
      setSubmitError("");
      const payload = toApiActivityPayload(activityForm, currentUser?.id);

      if (editingActivityId) {
        await updateActivity(editingActivityId, payload);
      } else {
        await createActivity(payload);
      }

      await loadSchedulingData();
      resetForm();
      setOpenFormSlot(null);
    } catch (submitActivityError) {
      setSubmitError(getApiErrorMessage(submitActivityError, "Unable to save this activity right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateLocation = async () => {
    if (!activePlan?.destinationId) {
      setLocationError("Select a plan with a destination before adding a location.");
      return;
    }

    try {
      setSavingLocation(true);
      setLocationError("");

      const newLocation = await createLocation({
        name: locationForm.name.trim(),
        address: locationForm.address.trim(),
        type: locationForm.type,
        destinationId: Number(activePlan.destinationId),
      });

      setLocations((currentLocations) => [...currentLocations, newLocation]);
      setActivityForm((currentForm) => ({
        ...currentForm,
        locationId: String(newLocation.id),
      }));
      resetLocationForm();
      setShowLocationForm(false);
    } catch (createLocationError) {
      setLocationError(getApiErrorMessage(createLocationError, "Unable to add this location right now."));
    } finally {
      setSavingLocation(false);
    }
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to see its generated daily structure and scheduled activities." />;
  if (loading && !days.length) {
    return <ModuleLoading label="Loading daily schedule..." />;
  }
  if (error) return <ModuleError message={error} />;
  if (!days.length) return <ModuleEmpty title="No day structure available" description="Create a plan first so your trip days can be organized into a schedule." />;

  return (
    <div className="max-w-7xl mx-auto">
      <div className="space-y-6">
        <div className="space-y-6">
          {conflicts.length > 0 ? (
            <div className="bg-yellow-50 border border-yellow-300 rounded-lg p-4">
              <div className="flex items-start gap-3">
                <AlertCircle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
                <div>
                  <h3 className="font-medium text-yellow-900">Schedule Conflicts Detected</h3>
                  <ul className="mt-2 space-y-1 text-sm text-yellow-700">
                    {conflicts.map((conflict, index) => (
                      <li key={index}>Day {conflict.day} - {conflict.slot}: {conflict.message}</li>
                    ))}
                  </ul>
                </div>
              </div>
            </div>
          ) : null}

          {days.map((day, index) => (
            <div key={day.id} className="bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm">
              <div className="bg-gray-50 border-b border-gray-200 px-4 py-2.5">
                <h2 className="font-medium flex items-center gap-2">
                  <Calendar className="w-5 h-5" />
                  Day {index + 1} - {new Date(day.date).toLocaleDateString()}
                </h2>
              </div>

              <div className="p-4 space-y-3">
                {timeSlots.map((slot) => {
                  const activities = (day.activities || []).filter((activity) => activity.timeslot === slot.id);
                  const hasConflict = conflicts.some((conflict) => conflict.day === index + 1 && conflict.slot === slot.label.toLowerCase());

                  return (
                    <div key={slot.id} className={`border rounded-xl p-3 ${hasConflict ? "border-yellow-400 bg-yellow-50" : "border-gray-300"}`}>
                      <div className="flex items-center justify-between mb-3">
                        <div className="flex items-center gap-2">
                          <div className={`w-3 h-3 rounded-full ${
                            slot.color === "yellow" ? "bg-yellow-400" : slot.color === "blue" ? "bg-blue-400" : "bg-purple-400"
                          }`} />
                          <h3 className="font-medium">{slot.label}</h3>
                          <span className="text-sm text-gray-600">({slot.time})</span>
                        </div>
                        <Clock className="w-4 h-4 text-gray-400" />
                      </div>
                      <button
                        type="button"
                        onClick={() => handleOpenForm(day.id, slot.id)}
                        className="w-full mb-3 text-sm px-3 py-2 border border-dashed border-gray-400 rounded-lg hover:bg-gray-50 transition"
                      >
                        + Add Activity
                      </button>
                      {openFormSlot?.dayId === day.id &&
                      openFormSlot?.timeslot === slot.id && (
                        <form
                          onSubmit={handleSubmit}
                          className="mb-4 border border-blue-300 bg-white shadow-sm rounded-xl p-4 space-y-3"
                        >
                          <div className="flex items-center justify-between text-xs text-gray-500">
                            <span>
                              Day {index + 1} · {slot.label}
                            </span>

                            <span>{slot.time}</span>
                          </div>
                          <input
                            type="text"
                            value={activityForm.name}
                            onChange={(event) =>
                              setActivityForm({
                                ...activityForm,
                                name: event.target.value,
                              })
                            }
                            placeholder="Activity name"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                          />

                          <select
                            value={activityForm.locationId}
                            onChange={(event) =>
                              setActivityForm({
                                ...activityForm,
                                locationId: event.target.value,
                              })
                            }
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-white"
                          >
                            <option value="">Select location</option>

                            {locations.map((location) => (
                              <option key={location.id} value={location.id}>
                                {location.name}
                              </option>
                            ))}
                          </select>
                          <button
                            type="button"
                            onClick={() => {
                              setLocationError("");
                              setShowLocationForm(!showLocationForm);
                            }}
                            className="text-sm text-blue-600 hover:text-blue-700 text-left"
                          >
                            + Add new location
                          </button>
                          {showLocationForm ? (
                            <div className="border border-gray-200 rounded-xl p-3 space-y-3 bg-gray-50">

                              <input
                                type="text"
                                value={locationForm.name}
                                onChange={(event) =>
                                  setLocationForm({
                                    ...locationForm,
                                    name: event.target.value,
                                  })
                                }
                                placeholder="Location name"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                              />

                              <input
                                type="text"
                                value={locationForm.address}
                                onChange={(event) =>
                                  setLocationForm({
                                    ...locationForm,
                                    address: event.target.value,
                                  })
                                }
                                placeholder="Address"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                              />

                              <select
                                value={locationForm.type}
                                onChange={(event) =>
                                  setLocationForm({
                                    ...locationForm,
                                    type: event.target.value,
                                  })
                                }
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-white"
                              >
                                <option value="ATTRACTION">Attraction</option>
                                <option value="RESTAURANT">Restaurant</option>
                                <option value="HOTEL">Hotel</option>
                                <option value="CAFE">Cafe</option>
                              </select>

                              {locationError ? (
                                <div className="text-sm text-red-600">
                                  {locationError}
                                </div>
                              ) : null}

                              <div className="flex gap-2">
                                <button
                                  type="button"
                                  onClick={handleCreateLocation}
                                  disabled={savingLocation}
                                  className="px-4 py-2 bg-gray-900 text-white rounded-lg"
                                >
                                  {savingLocation ? "Saving..." : "Save location"}
                                </button>

                                <button
                                  type="button"
                                  onClick={() => {
                                    setShowLocationForm(false);
                                    resetLocationForm();
                                  }}
                                  className="px-4 py-2 border border-gray-300 rounded-lg"
                                >
                                  Cancel
                                </button>
                              </div>
                            </div>
                          ) : null}
                          <div className="grid grid-cols-2 gap-3">
                            <input
                              type="time"
                              min={TIMESLOT_LIMITS[slot.id].start}
                              max={TIMESLOT_LIMITS[slot.id].end}
                              value={activityForm.startTime}
                              onChange={(event) => {
                                setSubmitError("");
                                setActivityForm({
                                  ...activityForm,
                                  startTime: event.target.value,
                                });
                              }}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                            />

                            <input
                              type="time"
                              min={TIMESLOT_LIMITS[slot.id].start}
                              max={TIMESLOT_LIMITS[slot.id].end}
                              value={activityForm.endTime}
                              onChange={(event) => {
                                setSubmitError("");

                                setActivityForm({
                                  ...activityForm,
                                  endTime: event.target.value,
                                });
                              }}
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                            />
                          </div>
                          <div className="grid grid-cols-2 gap-3">
                            <select
                              value={activityForm.status}
                              onChange={(event) =>
                                setActivityForm({
                                  ...activityForm,
                                  status: event.target.value,
                                })
                              }
                              className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-white"
                            >
                              <option value="PLANNED">Planned</option>
                              <option value="CONFIRMED">Confirmed</option>
                              <option value="COMPLETED">Completed</option>
                            </select>
                          </div>
                          <textarea
                            rows={2}
                            value={activityForm.description}
                            onChange={(event) =>
                              setActivityForm({
                                ...activityForm,
                                description: event.target.value,
                              })
                            }
                            placeholder="Notes"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                          />
                          {submitError ? (
                            <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                              {submitError}
                            </div>
                          ) : null}
                          <div className="flex gap-2">
                            <button
                              type="submit"
                              className="px-4 py-2 bg-blue-500 text-white rounded-lg"
                            >
                              Save Activity
                            </button>

                            <button
                              type="button"
                              onClick={() => {
                                setOpenFormSlot(null);
                                resetForm();
                              }}
                              className="px-4 py-2 border border-gray-300 rounded-lg"
                            >
                              Cancel
                            </button>
                          </div>
                        </form>
                      )}
                      {activities.length > 0 ? (
                        <div className="space-y-2">
                          {activities.map((activity) => {
                            if (editingActivityId === activity.id) {
                              return null;
                            }
                            return (
                              <div key={activity.id} className="border border-gray-200 rounded-xl p-3 bg-white shadow-sm hover:border-gray-300 transition flex justify-between items-center gap-4">
                                <div>
                                  <p className="text-sm font-medium">{activity.name}</p>
                                  <p className="text-xs text-gray-600">{activity.locationName || "Location pending"}</p>
                                  {activity.description ? <p className="text-xs text-gray-500 mt-1">{activity.description}</p> : null}
                                </div>
                                <div className="flex items-center gap-2">
                                  <div className="text-xs text-gray-500 text-right">
                                    <p>{activity.duration || 0} min</p>
                                    <p>{activity.status || "PLANNED"}</p>
                                  </div>
                                  <button
                                    type="button"
                                    onClick={() => handleEdit(activity)}
                                    className="p-2 border border-gray-300 rounded hover:bg-gray-50"
                                    aria-label={`Edit ${activity.name}`}
                                  >
                                    <Pencil className="w-4 h-4 text-gray-600" />
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => handleDelete(activity.id)}
                                    className="p-2 border border-gray-300 rounded hover:bg-red-50"
                                    aria-label={`Delete ${activity.name}`}
                                  >
                                    <Trash2 className="w-4 h-4 text-red-600" />
                                  </button>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      ) : (
                        <div className="border-2 border-dashed border-gray-300 rounded p-4 text-center text-gray-400 text-sm">
                          No activities scheduled in this slot yet
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
