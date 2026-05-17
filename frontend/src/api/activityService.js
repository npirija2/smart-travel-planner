import api from './api';

export const getActivities = async () => {
  const response = await api.get('/activities');
  return response.data;
};

export const getActivitiesByDay = async (dayId) => {
  const response = await api.get(`/activities/day/${dayId}`);
  return response.data;
};

export const createActivity = async (activityData) => {
  const response = await api.post('/activities', activityData);
  return response.data;
};

export const updateActivity = async (activityId, activityData) => {
  const response = await api.put(`/activities/${activityId}`, activityData);
  return response.data;
};

export const deleteActivity = async (activityId) => {
  await api.delete(`/activities/${activityId}`);
};
