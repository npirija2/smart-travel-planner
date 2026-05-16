import { api } from './api';

export async function getNotificationsByUserId(userId) {
  const response = await api.get(`/notifications/user/${userId}`);
  return response.data;
}

export async function createNotification(payload) {
  const response = await api.post('/notifications', payload);
  return response.data;
}

export async function deleteNotification(id) {
  await api.delete(`/notifications/${id}`);
}

export async function getVotesByActivityId(activityId) {
  const response = await api.get(`/votes/activity/${activityId}`);
  return response.data;
}

export async function createVote(payload) {
  const response = await api.post('/votes', payload);
  return response.data;
}

export async function deleteVote(id) {
  await api.delete(`/votes/${id}`);
}

export async function getSharedLinksByPlanId(planId) {
  const response = await api.get(`/shared-links/plan/${planId}`);
  return response.data;
}

export async function createSharedLink(payload) {
  const response = await api.post('/shared-links', payload);
  return response.data;
}

export async function updateSharedLink(id, payload) {
  const response = await api.put(`/shared-links/${id}`, payload);
  return response.data;
}

export async function deleteSharedLink(id) {
  await api.delete(`/shared-links/${id}`);
}
