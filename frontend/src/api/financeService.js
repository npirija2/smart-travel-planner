import { api } from './api';

export async function getBudgetsByPlanId(planId) {
  const response = await api.get(`/budgets/plan/${planId}`);
  return response.data;
}

export async function createBudget(payload) {
  const response = await api.post('/budgets', payload);
  return response.data;
}

export async function updateBudget(id, payload) {
  const response = await api.put(`/budgets/${id}`, payload);
  return response.data;
}

export async function deleteBudget(id) {
  await api.delete(`/budgets/${id}`);
}

export async function getExpensesByPlanId(planId) {
  const response = await api.get(`/expenses/plan/${planId}`);
  return response.data;
}

export async function createExpense(payload) {
  const response = await api.post('/expenses', payload);
  return response.data;
}

export async function deleteExpense(id) {
  await api.delete(`/expenses/${id}`);
}

export async function getPagedReservations(planId, page = 0, size = 10) {
  const response = await api.get(`/reservations/plan/${planId}/paged`, {
    params: {
      page,
      size,
      sortBy: 'price',
      direction: 'desc',
    },
  });
  return response.data;
}

export async function getPremiumReservations(planId, minPrice) {
  const response = await api.get(`/reservations/plan/${planId}/premium`, {
    params: { minPrice },
  });
  return response.data;
}

export async function createReservation(payload) {
  const response = await api.post('/reservations', payload);
  return response.data;
}

export async function deleteReservation(id) {
  await api.delete(`/reservations/${id}`);
}

export async function getSagaReservations(planId) {
  const response = await api.get(`/saga-reservations/plan/${planId}`);
  return response.data;
}
