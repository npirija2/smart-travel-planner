import api from './api';

export const getLocations = async () => {
  const response = await api.get('/locations');
  return response.data;
};

export const getLocationsByDestination = async (destinationId) => {
  const response = await api.get(`/locations/destination/${destinationId}`);
  return response.data;
};

export const createLocation = async (locationData) => {
  const response = await api.post('/locations', locationData);
  return response.data;
};
