import api from './api';

export const getDestinations = async () => {
    const response = await api.get('/destinations');
    return response.data;
};

export const createDestination = async (destinationData) => {
    const response = await api.post('/destinations', destinationData);
    return response.data;
};
