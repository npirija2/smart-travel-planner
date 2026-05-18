import { MessageSquare, Star, Vote } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { createVote, deleteVote, getVotesByActivity } from "../../api/voteService";
import { createReview, getReviewsByActivity } from "../../api/reviewService";
import { getApiErrorMessage } from "../../api/errorUtils";
import { getTravelPlanDays } from "../../api/planService";
import { useAuth } from "../context/AuthContext";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function ActivityVoting() {
  const { currentUser } = useAuth();
  const { activePlan } = usePlanContext();
  const [days, setDays] = useState([]);
  const [selectedActivityId, setSelectedActivityId] = useState("");
  const [votes, setVotes] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewForm, setReviewForm] = useState({ rating: "5", comment: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const activities = useMemo(
    () =>
      days.flatMap((day) =>
        (day.activities || []).map((activity) => ({
          ...activity,
          dayDate: day.date,
        })),
      ),
    [days],
  );

  const selectedActivity = activities.find((activity) => String(activity.id) === String(selectedActivityId)) || null;

  async function loadActivities() {
    if (!activePlan) {
      setDays([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const dayData = await getTravelPlanDays(activePlan.id);
      setDays(dayData);
      if (dayData.length > 0) {
        const firstActivity = dayData.flatMap((day) => day.activities || [])[0];
        setSelectedActivityId((currentSelected) => currentSelected || String(firstActivity?.id || ""));
      } else {
        setSelectedActivityId("");
      }
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, "Unable to load plan activities right now."));
    } finally {
      setLoading(false);
    }
  }

  async function loadVotesAndReviews(activityId) {
    if (!activityId) {
      setVotes([]);
      setReviews([]);
      return;
    }

    try {
      const [voteData, reviewData] = await Promise.all([
        getVotesByActivity(Number(activityId)),
        getReviewsByActivity(Number(activityId)),
      ]);
      setVotes(voteData);
      setReviews(reviewData);
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, "Unable to load voting details right now."));
    }
  }

  useEffect(() => {
    loadActivities();
  }, [activePlan?.id]);

  useEffect(() => {
    loadVotesAndReviews(selectedActivityId);
  }, [selectedActivityId]);

  const currentVote = votes.find((vote) => Number(vote.userId) === Number(currentUser?.id));

  const submitVote = async () => {
    if (!currentUser || !selectedActivityId || currentVote) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      await createVote({
        userId: Number(currentUser.id),
        activityId: Number(selectedActivityId),
      });
      await loadVotesAndReviews(selectedActivityId);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to record your vote right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const removeVote = async () => {
    if (!currentVote) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      await deleteVote(currentVote.id);
      await loadVotesAndReviews(selectedActivityId);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to remove your vote right now."));
    } finally {
      setSubmitting(false);
    }
  };

  const submitReview = async () => {
    if (!currentUser || !selectedActivityId || !reviewForm.comment.trim()) {
      return;
    }

    try {
      setSubmitting(true);
      setError("");
      await createReview({
        userId: Number(currentUser.id),
        activityId: Number(selectedActivityId),
        rating: Number(reviewForm.rating),
        comment: reviewForm.comment.trim(),
      });
      setReviewForm({ rating: "5", comment: "" });
      await loadVotesAndReviews(selectedActivityId);
    } catch (submitError) {
      setError(getApiErrorMessage(submitError, "Unable to submit your review right now."));
    } finally {
      setSubmitting(false);
    }
  };

  if (!activePlan) {
    return <ModuleEmpty title="No active plan selected" description="Choose a plan to vote on activities and collect group feedback." />;
  }

  if (loading) {
    return <ModuleLoading label="Loading activity voting..." />;
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center">
            <Vote className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-medium mb-2">Activity Voting</h1>
            <p className="text-gray-600">Help the group decide what stays in the itinerary by voting and leaving quick feedback on planned activities.</p>
          </div>
        </div>
      </div>

      {error ? <div className="mb-6"><ModuleError message={error} /></div> : null}

      {!activities.length ? (
        <ModuleEmpty
          title="No activities available yet"
          description="Add activities to your schedule first, then your group can vote and leave feedback on them here."
        />
      ) : (
        <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
          <div className="xl:col-span-1">
            <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
              <h2 className="font-medium mb-4">Choose Activity</h2>
              <div className="space-y-3">
                <select
                  value={selectedActivityId}
                  onChange={(event) => setSelectedActivityId(event.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded bg-white focus:outline-none focus:border-blue-500"
                >
                  <option value="">Select an activity</option>
                  {activities.map((activity) => (
                    <option key={activity.id} value={activity.id}>
                      {activity.name} · {new Date(activity.dayDate).toLocaleDateString()}
                    </option>
                  ))}
                </select>

                {selectedActivity ? (
                  <div className="border border-gray-300 rounded p-4">
                    <p className="font-medium">{selectedActivity.name}</p>
                    <p className="text-sm text-gray-600 mt-1">
                      {selectedActivity.locationName || "Location pending"} · {selectedActivity.timeslot}
                    </p>
                    {selectedActivity.description ? <p className="text-sm text-gray-500 mt-2">{selectedActivity.description}</p> : null}
                  </div>
                ) : null}

                {currentVote ? (
                  <button
                    type="button"
                    disabled={submitting}
                    onClick={removeVote}
                    className="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-60"
                  >
                    Remove my vote
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={submitting || !selectedActivityId}
                    onClick={submitVote}
                    className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 disabled:opacity-60"
                  >
                    Vote for activity
                  </button>
                )}
              </div>
            </div>
          </div>

          <div className="xl:col-span-2 space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
                <div className="flex items-center gap-2 mb-2">
                  <Vote className="w-4 h-4 text-blue-600" />
                  <span className="text-sm text-gray-600">Votes</span>
                </div>
                <p className="text-3xl font-medium">{votes.length}</p>
              </div>
              <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
                <div className="flex items-center gap-2 mb-2">
                  <MessageSquare className="w-4 h-4 text-blue-600" />
                  <span className="text-sm text-gray-600">Reviews</span>
                </div>
                <p className="text-3xl font-medium">{reviews.length}</p>
              </div>
              <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
                <div className="flex items-center gap-2 mb-2">
                  <Star className="w-4 h-4 text-blue-600" />
                  <span className="text-sm text-gray-600">Average rating</span>
                </div>
                <p className="text-3xl font-medium">
                  {reviews.length ? (reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length).toFixed(1) : "0.0"}
                </p>
              </div>
            </div>

            <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
              <h2 className="font-medium mb-4">Leave Feedback</h2>
              <div className="grid grid-cols-1 md:grid-cols-[160px_1fr_auto] gap-3">
                <select
                  value={reviewForm.rating}
                  onChange={(event) => setReviewForm({ ...reviewForm, rating: event.target.value })}
                  className="px-3 py-2 border border-gray-300 rounded bg-white focus:outline-none focus:border-blue-500"
                >
                  <option value="5">5 stars</option>
                  <option value="4">4 stars</option>
                  <option value="3">3 stars</option>
                  <option value="2">2 stars</option>
                  <option value="1">1 star</option>
                </select>
                <input
                  value={reviewForm.comment}
                  onChange={(event) => setReviewForm({ ...reviewForm, comment: event.target.value })}
                  placeholder="Share a quick reason or suggestion"
                  className="px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                />
                <button
                  type="button"
                  disabled={submitting || !selectedActivityId || !reviewForm.comment.trim()}
                  onClick={submitReview}
                  className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 disabled:opacity-60"
                >
                  Submit
                </button>
              </div>
            </div>

            <div className="bg-white border-2 border-gray-300 rounded-lg p-5">
              <h2 className="font-medium mb-4">Recent Feedback</h2>
              <div className="space-y-3">
                {reviews.length > 0 ? reviews.map((review) => (
                  <div key={review.id} className="border border-gray-300 rounded p-4 flex justify-between items-start gap-4">
                    <div>
                      <p className="font-medium">User {review.userId}</p>
                      <p className="text-sm text-gray-600 mt-1">{review.comment}</p>
                    </div>
                    <span className="px-3 py-1 text-xs border border-gray-300 rounded bg-gray-50">{review.rating}/5</span>
                  </div>
                )) : (
                  <div className="border-2 border-dashed border-gray-300 rounded p-4 text-sm text-gray-500">
                    No feedback yet for this activity.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
