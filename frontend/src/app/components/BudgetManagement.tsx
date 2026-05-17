import { DollarSign, PieChart, Plus, TrendingUp, AlertCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createBudget, getBudgetsByPlan } from "../../api/budgetService";
import { createExpense, getExpensesByPlan } from "../../api/expenseService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

export function BudgetManagement() {
  const { activePlan } = usePlanContext();
  const [budgets, setBudgets] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [budgetForm, setBudgetForm] = useState({ totalAmount: "", currency: "EUR" });
  const [expenseForm, setExpenseForm] = useState({ amount: "", category: "", date: "" });

  const loadBudgetData = async () => {
    if (!activePlan) {
      setBudgets([]);
      setExpenses([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const [nextBudgets, nextExpenses] = await Promise.all([
        getBudgetsByPlan(activePlan.id),
        getExpensesByPlan(activePlan.id),
      ]);
      setBudgets(nextBudgets);
      setExpenses(nextExpenses);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to load budget information."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBudgetData();
  }, [activePlan?.id]);

  const totals = useMemo(() => {
    const totalBudget = budgets.reduce((sum, budget) => sum + (budget.totalAmount || 0), 0);
    const totalSpent = expenses.reduce((sum, expense) => sum + (expense.amount || 0), 0);
    return {
      totalBudget,
      totalSpent,
      remaining: totalBudget - totalSpent,
    };
  }, [budgets, expenses]);

  const categories = useMemo(() => {
    return Object.values(
      expenses.reduce((accumulator, expense) => {
        const category = expense.category || "Uncategorized";
        accumulator[category] = accumulator[category] || { name: category, spent: 0 };
        accumulator[category].spent += expense.amount || 0;
        return accumulator;
      }, {}),
    );
  }, [expenses]);

  const handleCreateBudget = async (event) => {
    event.preventDefault();
    await createBudget({
      totalAmount: Number(budgetForm.totalAmount),
      planId: activePlan.id,
      currency: budgetForm.currency,
    });
    setBudgetForm({ totalAmount: "", currency: budgetForm.currency });
    await loadBudgetData();
  };

  const handleCreateExpense = async (event) => {
    event.preventDefault();
    await createExpense({
      amount: Number(expenseForm.amount),
      planId: activePlan.id,
      category: expenseForm.category,
      date: expenseForm.date ? new Date(expenseForm.date).toISOString() : new Date().toISOString(),
    });
    setExpenseForm({ amount: "", category: "", date: "" });
    await loadBudgetData();
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan to manage its budget and expenses." />;
  if (loading) return <ModuleLoading label="Loading budget data..." />;
  if (error) return <ModuleError message={error} />;

  return (
    <div className="max-w-7xl mx-auto">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Total Budget</p>
            <DollarSign className="w-5 h-5 text-gray-400" />
          </div>
          <p className="text-3xl font-medium">{totals.totalBudget.toFixed(2)}</p>
        </div>

        <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Spent</p>
            <TrendingUp className="w-5 h-5 text-red-500" />
          </div>
          <p className="text-3xl font-medium text-red-600">{totals.totalSpent.toFixed(2)}</p>
        </div>

        <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
          <div className="flex items-center justify-between mb-2">
            <p className="text-sm text-gray-600">Remaining</p>
            <DollarSign className="w-5 h-5 text-green-500" />
          </div>
          <p className="text-3xl font-medium text-green-600">{totals.remaining.toFixed(2)}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <PieChart className="w-5 h-5" />
              Expense Categories
            </h2>

            {categories.length === 0 ? (
              <ModuleEmpty title="No expenses yet" description="Start adding expenses to see category breakdown." />
            ) : (
              <div className="space-y-4">
                {categories.map((category) => (
                  <div key={category.name} className="border border-gray-300 rounded p-4">
                    <div className="flex justify-between items-center">
                      <p className="font-medium">{category.name}</p>
                      <p className="text-sm text-gray-600">{category.spent.toFixed(2)}</p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <AlertCircle className="w-5 h-5" />
              Expenses
            </h2>

            {expenses.length === 0 ? (
              <ModuleEmpty title="No expenses recorded" description="Create expenses from the form to start tracking trip spending here." />
            ) : (
              <div className="space-y-3">
                {expenses.map((expense) => (
                  <div key={expense.id} className="border border-gray-300 rounded p-4 flex justify-between items-start gap-4">
                    <div>
                      <p className="font-medium">{expense.category || "Uncategorized"}</p>
                      <p className="text-sm text-gray-600">{new Date(expense.date).toLocaleString()}</p>
                    </div>
                    <p className="font-medium">{expense.amount.toFixed(2)}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <form onSubmit={handleCreateBudget} className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3">
            <h3 className="font-medium flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Add Budget
            </h3>
            <input
              required
              type="number"
              min="0"
              step="0.01"
              value={budgetForm.totalAmount}
              onChange={(event) => setBudgetForm({ ...budgetForm, totalAmount: event.target.value })}
              placeholder="Total amount"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />
            <input
              value={budgetForm.currency}
              onChange={(event) => setBudgetForm({ ...budgetForm, currency: event.target.value })}
              placeholder="Currency"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />
            <button className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600">
              Save Budget
            </button>
          </form>

          <form onSubmit={handleCreateExpense} className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3">
            <h3 className="font-medium flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Add Expense
            </h3>
            <input
              required
              type="number"
              min="0"
              step="0.01"
              value={expenseForm.amount}
              onChange={(event) => setExpenseForm({ ...expenseForm, amount: event.target.value })}
              placeholder="Amount"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />
            <input
              required
              value={expenseForm.category}
              onChange={(event) => setExpenseForm({ ...expenseForm, category: event.target.value })}
              placeholder="Category"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />
            <input
              type="datetime-local"
              value={expenseForm.date}
              onChange={(event) => setExpenseForm({ ...expenseForm, date: event.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />
            <button className="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-50">
              Save Expense
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
