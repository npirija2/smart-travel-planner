import api from './api';

export const getPlanMembershipsForUser = async (userId) => {
  const response = await api.get(`/users/${userId}/plan-memberships`);
  return response.data;
};

export const createPlanMembership = async (userId, membershipData) => {
  const response = await api.post(`/users/${userId}/plan-memberships`, membershipData);
  return response.data;
};

export const deletePlanMembership = async (membershipId) => {
  await api.delete(`/plan-memberships/${membershipId}`);
};
