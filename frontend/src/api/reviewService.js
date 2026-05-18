import api from './api';

export const getReviewsByActivity = async (activityId) => {
  const response = await api.get(`/reviews/activity/${activityId}`);
  return response.data;
};

export const createReview = async (reviewData) => {
  const response = await api.post('/reviews', reviewData);
  return response.data;
};
