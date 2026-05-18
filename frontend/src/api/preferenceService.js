import api from './api';

export const getUserPreferences = async (userId) => {
  const response = await api.get(`/users/${userId}/preferences`);
  return response.data;
};

export const createUserPreference = async (userId, preferenceData) => {
  const response = await api.post(`/users/${userId}/preferences`, preferenceData);
  return response.data;
};
