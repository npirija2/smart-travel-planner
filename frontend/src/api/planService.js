import api from './api';

export const getTravelPlans = async () => {
    const response = await api.get('/travel-plans');
    return response.data;
};

export const createTravelPlan = async (planData) => {
    const response = await api.post('/travel-plans', planData);
    return response.data;
};

export const getTravelPlanById = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}`);
    return response.data;
};

export const updateTravelPlan = async (planId, planData) => {
    const response = await api.put(`/travel-plans/${planId}`, planData);
    return response.data;
};

export const deleteTravelPlan = async (planId) => {
    await api.delete(`/travel-plans/${planId}`);
};

export const getTravelPlanDays = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}/days`);
    return response.data;
};

export const getRouteOptimization = async (travelPlanId) => {
  const response = await api.get(`/routes/optimize/${travelPlanId}`);
  return response.data;
};

export const getScheduleLoad = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}/schedule-load`);
    return response.data;
};

export const getAttractions = async (planId, interest = '') => {
    const response = await api.get(`/travel-plans/${planId}/attractions`, {
        params: { interest },
    });
    return response.data;
};

export const getWeatherForecast = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}/weather`);
    return response.data;
};

export const getLocalRecommendations = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}/local-recommendations`);
    return response.data;
};

export const getWaitingTimeInsights = async (planId) => {
    const response = await api.get(`/travel-plans/${planId}/waiting-times`);
    return response.data;
};
