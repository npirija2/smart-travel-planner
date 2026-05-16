import { api, clearSession, getStoredSession, storeSession } from './api';

export async function loginUser(email, password) {
  const response = await api.post('/users/login', { email, password });
  return response.data;
}

export async function registerUser(payload) {
  const response = await api.post('/users', payload);
  return response.data;
}

export async function refreshAccessToken(refreshToken) {
  const response = await api.post('/users/refresh', { refreshToken });
  return response.data;
}

export async function getCurrentUserProfile() {
  const response = await api.get('/users/me');
  return response.data;
}

export { clearSession, getStoredSession, storeSession };
