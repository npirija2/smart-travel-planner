import api from './api';

export const getNotifications = async () => {
  const response = await api.get('/notifications');
  return response.data;
};

export const getNotificationsByPlan = async (planId) => {
  const response = await api.get(`/notifications/plan/${planId}`);
  return response.data;
};

export const createNotification = async (notificationData) => {
  const response = await api.post('/notifications', notificationData);
  return response.data;
};

export const deleteNotification = async (notificationId) => {
  await api.delete(`/notifications/${notificationId}`);
};
