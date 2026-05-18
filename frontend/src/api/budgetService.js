import api from './api';

export const getBudgetsByPlan = async (planId) => {
  const response = await api.get(`/budgets/plan/${planId}`);
  if (!response.data) {
    return [];
  }

  return Array.isArray(response.data) ? response.data : [response.data];
};

export const createBudget = async (budgetData) => {
  const response = await api.post('/budgets', budgetData);
  return response.data;
};

export const updateBudget = async (budgetId, budgetData) => {
  const response = await api.put(`/budgets/${budgetId}`, budgetData);
  return response.data;
};

export const deleteBudget = async (budgetId) => {
  await api.delete(`/budgets/${budgetId}`);
};

export const estimateBudget = async (planId) => {
  const response = await api.get(`/budgets/estimate/${planId}`);
  return response.data;
}
