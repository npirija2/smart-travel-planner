import api from './api';

export const getTravelPlans = async () => {
    const response = await api.get('/travel-plans');
    return response.data;
};

export const createTravelPlan = async (planData) => {
    const response = await api.post('/travel-plans', planData);
    return response.data;
};