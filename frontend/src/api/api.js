import axios from 'axios';

const ACTIVE_PLAN_STORAGE_KEY = 'active-plan-id';

function clearClientSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem(ACTIVE_PLAN_STORAGE_KEY);
  sessionStorage.clear();
}

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        const refreshToken =
          localStorage.getItem('refreshToken');

        const response = await axios.post(
          'http://localhost:8080/api/users/refresh',
          {
            refreshToken,
          }
        );

        const newAccessToken =
          response.data.accessToken;

        localStorage.setItem(
          'token',
          newAccessToken
        );

        originalRequest.headers.Authorization =
          `Bearer ${newAccessToken}`;

        return api(originalRequest);

      } catch (refreshError) {
        clearClientSession();

        window.location.href = '/login';

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
