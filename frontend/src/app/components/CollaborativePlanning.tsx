import { Link2, Users, UserPlus, Calendar } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getTravelPlanDays } from "../../api/planService";
import { createPlanMembership, deletePlanMembership, getPlanMembershipsForUser } from "../../api/planUserService";
import { createSharedLink, getSharedLinksByPlan } from "../../api/sharedLinkService";
import { getAllUsers } from "../../api/userService";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function CollaborativePlanning() {
  const { currentUser } = useAuth();
  const { activePlan } = usePlanContext();
  const [users, setUsers] = useState([]);
  const [memberships, setMemberships] = useState([]);
  const [sharedLinks, setSharedLinks] = useState([]);
  const [planDays, setPlanDays] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState("");
  const [role, setRole] = useState("EDITOR");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const proposedActivities = useMemo(
    () =>
      planDays.flatMap((day) =>
        (day.activities || []).map((activity) => ({
          ...activity,
          dayDate: day.date,
        })),
      ),
    [planDays],
  );

  async function loadCollaborationData() {
    if (!activePlan) {
      setUsers([]);
      setMemberships([]);
      setSharedLinks([]);
      setPlanDays([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const [userList, linkList, days] = await Promise.all([
        getAllUsers(),
        getSharedLinksByPlan(activePlan.id),
        getTravelPlanDays(activePlan.id),
      ]);

      setUsers(userList);
      setSharedLinks(linkList);
      setPlanDays(days);

      const membershipGroups = await Promise.all(
        userList.map(async (user) => ({
          user,
          memberships: await getPlanMembershipsForUser(user.id),
        })),
      );

      const planMemberships = membershipGroups
        .flatMap((entry) =>
          entry.memberships
            .filter((membership) => String(membership.planId) === String(activePlan.id))
            .map((membership) => ({ ...membership, user: entry.user })),
        )
        .sort((left, right) => String(left.user?.username || "").localeCompare(String(right.user?.username || "")));

      setMemberships(planMemberships);
      if (userList.length > 0) {
        setSelectedUserId((currentSelected) => currentSelected || String(userList[0].id));
      }
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, "Unable to load collaboration data right now."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCollaborationData();
  }, [activePlan?.id]);

  const addMember = async () => {
    if (!activePlan || !selectedUserId) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      await createPlanMembership(Number(selectedUserId), {
        planId: Number(activePlan.id),
        role,
      });
      await loadCollaborationData();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to add this collaborator right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const removeMember = async (membershipId) => {
    try {
      setSubmitting(true);
      setError("");
      await deletePlanMembership(membershipId);
      await loadCollaborationData();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to remove this collaborator right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const createInviteLink = async () => {
    if (!activePlan) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      await createSharedLink({
        url: `${window.location.origin}/plans/${activePlan.id}`,
        planId: Number(activePlan.id),
        type: "VIEW",
      });
      await loadCollaborationData();
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to create a share link right now."));
    } finally {
      setSubmitting(false);
    }
  };

  if (!activePlan) {
    return <ModuleEmpty title="No active plan selected" description="Choose a plan to manage collaborators, links, and shared activity proposals." />;
  }

  if (loading) {
    return <ModuleLoading label="Loading collaboration workspace..." />;
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center">
            <Users className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-medium mb-2">Collaborative Planning</h1>
            <p className="text-gray-600">Invite collaborators, assign roles, and keep the plan aligned around the activities already on the itinerary.</p>
          </div>
        </div>
      </div>

      {error ? <div className="mb-6"><ModuleError message={error} /></div> : null}

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="xl:col-span-1 space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
            <h2 className="font-medium mb-4 flex items-center gap-2">
              <UserPlus className="w-4 h-4" />
              Add Collaborator
            </h2>
            <div className="space-y-3">
              <select
                value={selectedUserId}
                onChange={(event) => setSelectedUserId(event.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded bg-white focus:outline-none focus:border-blue-500"
              >
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.username} ({user.email})
                  </option>
                ))}
              </select>
              <select
                value={role}
                onChange={(event) => setRole(event.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded bg-white focus:outline-none focus:border-blue-500"
              >
                <option value="EDITOR">Editor</option>
                <option value="VIEWER">Viewer</option>
                <option value="COORDINATOR">Coordinator</option>
              </select>
              <button
                type="button"
                disabled={submitting || !selectedUserId}
                onClick={addMember}
                className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 disabled:opacity-60"
              >
                Add collaborator
              </button>
            </div>
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
            <div className="flex items-center justify-between gap-3 mb-4">
              <h2 className="font-medium flex items-center gap-2">
                <Link2 className="w-4 h-4" />
                Share Links
              </h2>
              <button
                type="button"
                disabled={submitting}
                onClick={createInviteLink}
                className="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-60"
              >
                New link
              </button>
            </div>
            <div className="space-y-3">
              {sharedLinks.length > 0 ? sharedLinks.map((link) => (
                <div key={link.id} className="border border-gray-300 rounded p-3">
                  <p className="text-sm font-medium">{link.type}</p>
                  <p className="text-xs text-gray-600 break-all mt-1">{link.url}</p>
                </div>
              )) : (
                <div className="border-2 border-dashed border-gray-300 rounded p-4 text-sm text-gray-500">
                  Create a share link for teammates, family, or anyone reviewing the trip.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="xl:col-span-2 space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
            <h2 className="font-medium mb-4 flex items-center gap-2">
              <Users className="w-4 h-4" />
              Team Members
            </h2>
            <div className="space-y-3">
              {currentUser ? (
                <div className="border border-blue-300 bg-blue-50 rounded p-4 flex justify-between items-start gap-4">
                  <div>
                    <p className="font-medium">{currentUser.username}</p>
                    <p className="text-sm text-gray-600">{currentUser.email}</p>
                  </div>
                  <span className="px-3 py-1 text-xs border border-blue-400 rounded bg-white text-blue-700">Owner</span>
                </div>
              ) : null}

              {memberships.length > 0 ? memberships.map((membership) => (
                <div key={membership.id} className="border border-gray-300 rounded p-4 flex justify-between items-start gap-4">
                  <div>
                    <p className="font-medium">{membership.user?.username || `User ${membership.userId}`}</p>
                    <p className="text-sm text-gray-600">{membership.user?.email || "No email available"}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="px-3 py-1 text-xs border border-gray-300 rounded bg-gray-50">{membership.role}</span>
                    {Number(membership.userId) !== Number(currentUser?.id) ? (
                      <button
                        type="button"
                        disabled={submitting}
                        onClick={() => removeMember(membership.id)}
                        className="px-3 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-60"
                      >
                        Remove
                      </button>
                    ) : null}
                  </div>
                </div>
              )) : (
                <div className="border-2 border-dashed border-gray-300 rounded p-4 text-sm text-gray-500">
                  No collaborators have been added yet.
                </div>
              )}
            </div>
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
            <h2 className="font-medium mb-4 flex items-center gap-2">
              <Calendar className="w-4 h-4" />
              Activity Proposals
            </h2>
            <div className="space-y-3">
              {proposedActivities.length > 0 ? proposedActivities.map((activity) => (
                <div key={activity.id} className="border border-gray-300 rounded p-4">
                  <div className="flex justify-between items-start gap-4">
                    <div>
                      <p className="font-medium">{activity.name}</p>
                      <p className="text-sm text-gray-600">
                        {new Date(activity.dayDate).toLocaleDateString()} · {activity.timeslot} · {activity.locationName || "Location pending"}
                      </p>
                      {activity.description ? <p className="text-sm text-gray-500 mt-2">{activity.description}</p> : null}
                    </div>
                    <span className="px-3 py-1 text-xs border border-gray-300 rounded bg-gray-50">{activity.status || "PLANNED"}</span>
                  </div>
                </div>
              )) : (
                <div className="border-2 border-dashed border-gray-300 rounded p-4 text-sm text-gray-500">
                  Add activities to the schedule and they will appear here for the group to review together.
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
