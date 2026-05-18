import api from './api';

export const getReservations = async () => {
  const response = await api.get('/reservations');
  return response.data;
};

export const getReservationsForPlan = async (planId) => {
  const response = await api.get(`/reservations/plan/${planId}/paged`);
  return response.data;
};

export const getPremiumReservations = async (planId, minPrice) => {
  const response = await api.get(`/reservations/plan/${planId}/premium`, {
    params: { minPrice },
  });
  return response.data;
};

export const createReservation = async (reservationData) => {
  const response = await api.post('/reservations', reservationData);
  return response.data;
};

export const deleteReservation = async (reservationId) => {
  await api.delete(`/reservations/${reservationId}`);
};
