import axios from 'axios';

const SESSION_KEY = 'smart-travel-session';
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const api = axios.create({
  baseURL,
});

export function getStoredSession() {
  const raw = localStorage.getItem(SESSION_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function storeSession(tokens) {
  localStorage.setItem(SESSION_KEY, JSON.stringify(tokens));
}

export function clearSession() {
  localStorage.removeItem(SESSION_KEY);
}

function notifySessionExpired() {
  window.dispatchEvent(new CustomEvent('auth:expired'));
}

api.interceptors.request.use((config) => {
  const session = getStoredSession();

  if (session?.accessToken) {
    config.headers.Authorization = `Bearer ${session.accessToken}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const session = getStoredSession();

    if (
      error.response?.status === 401 &&
      session?.refreshToken &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/users/refresh')
    ) {
      originalRequest._retry = true;

      try {
        const refreshResponse = await axios.post(`${baseURL}/users/refresh`, {
          refreshToken: session.refreshToken,
        });

        storeSession(refreshResponse.data);
        originalRequest.headers.Authorization = `Bearer ${refreshResponse.data.accessToken}`;

        return api(originalRequest);
      } catch (refreshError) {
        clearSession();
        notifySessionExpired();
        if (refreshError.response?.data && typeof refreshError.response.data === 'object') {
          refreshError.response.data.message = 'Your session expired. Please sign in again.';
        }
        return Promise.reject(refreshError);
      }
    }

    if (error.response?.status === 401) {
      if (error.response?.data && typeof error.response.data === 'object') {
        error.response.data.message = 'Your session expired. Please sign in again.';
      }
      clearSession();
      notifySessionExpired();
    }

    return Promise.reject(error);
  },
);
