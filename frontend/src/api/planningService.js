import { api } from './api';

export async function getTravelPlans() {
  const response = await api.get('/travel-plans');
  return response.data;
}

export async function createTravelPlan(payload) {
  const response = await api.post('/travel-plans', payload);
  return response.data;
}

export async function updateTravelPlan(id, payload) {
  const response = await api.put(`/travel-plans/${id}`, payload);
  return response.data;
}

export async function deleteTravelPlan(id) {
  await api.delete(`/travel-plans/${id}`);
}

export async function requestSagaReservation(planId, payload) {
  const response = await api.post(`/travel-plans/${planId}/reservations`, payload);
  return response.data;
}

export async function getPlanReservations(planId) {
  const response = await api.get(`/travel-plans/${planId}/reservations`);
  return response.data;
}

export async function getDestinations() {
  const response = await api.get('/destinations');
  return response.data;
}

export async function createDestination(payload) {
  const response = await api.post('/destinations', payload);
  return response.data;
}

export async function getDays() {
  const response = await api.get('/days');
  return response.data;
}

export async function createDay(payload) {
  const response = await api.post('/days', payload);
  return response.data;
}

export async function getActivities() {
  const response = await api.get('/activities');
  return response.data;
}

export async function getActivitiesByDay(dayId) {
  const response = await api.get(`/activities/day/${dayId}`);
  return response.data;
}

export async function createActivity(payload) {
  const response = await api.post('/activities', payload);
  return response.data;
}

export async function addActivityToDay(dayId, payload) {
  const response = await api.post(`/activities/day/${dayId}`, payload);
  return response.data;
}

export async function updateActivity(activityId, payload) {
  const response = await api.put(`/activities/${activityId}`, payload);
  return response.data;
}

export async function deleteActivity(activityId) {
  await api.delete(`/activities/${activityId}`);
}

export async function getLocations() {
  const response = await api.get('/locations');
  return response.data;
}

export async function createLocation(payload) {
  const response = await api.post('/locations', payload);
  return response.data;
}
