import api from './api';

export const getExpensesByPlan = async (planId) => {
  const response = await api.get(`/expenses/plan/${planId}`);
  return response.data;
};

export const createExpense = async (expenseData) => {
  const response = await api.post('/expenses', expenseData);
  return response.data;
};

export const deleteExpense = async (expenseId) => {
  await api.delete(`/expenses/${expenseId}`);
};
