import { api } from './api';

export async function getAllUsers() {
  const response = await api.get('/users');
  return response.data;
}

export async function createPreference(userId, payload) {
  const response = await api.post(`/users/${userId}/preferences`, payload);
  return response.data;
}

export async function createBatchPreferences(userId, preferences) {
  const response = await api.post(`/users/${userId}/preferences/batch`, { preferences });
  return response.data;
}

export async function getUserPreferences(userId) {
  const response = await api.get(`/users/${userId}/preferences`);
  return response.data;
}

export async function createPlanMembership(userId, payload) {
  const response = await api.post(`/users/${userId}/plan-memberships`, payload);
  return response.data;
}

export async function getUserPlanMemberships(userId) {
  const response = await api.get(`/users/${userId}/plan-memberships`);
  return response.data;
}

export async function updatePlanMembership(planMembershipId, payload) {
  const response = await api.put(`/plan-memberships/${planMembershipId}`, payload);
  return response.data;
}

export async function deletePlanMembership(planMembershipId) {
  await api.delete(`/plan-memberships/${planMembershipId}`);
}
