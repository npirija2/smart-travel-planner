import api from './api';

export const getSharedLinksByPlan = async (planId) => {
  const response = await api.get(`/shared-links/plan/${planId}`);
  return response.data;
};

export const createSharedLink = async (sharedLinkData) => {
  const response = await api.post('/shared-links', sharedLinkData);
  return response.data;
};

export const deleteSharedLink = async (linkId) => {
  await api.delete(`/shared-links/${linkId}`);
};
