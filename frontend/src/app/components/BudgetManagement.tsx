import { AlertCircle, DollarSign, Pencil, PieChart, Plus, Trash2, TrendingUp, X } from "lucide-react";
import { type FormEvent, useEffect, useMemo, useState } from "react";
import { createBudget, deleteBudget, estimateBudget, getBudgetsByPlan, updateBudget } from "../../api/budgetService";
import { createExpense, getExpensesByPlan } from "../../api/expenseService";
import { getApiErrorMessage } from "../../api/errorUtils";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleLoading } from "./ModuleState";

type Budget = {
  id?: string;
  totalAmount: number;
  currency?: string;
  planId?: number;
};

type Expense = {
  id?: string;
  amount: number;
  category?: string;
  date: string;
  planId?: number;
};

type CategorySummary = {
  name: string;
  spent: number;
};

type BudgetEstimateResponse = {
  planId: number;
  destination: string;
  numberOfDays: number;
  accommodationCost: number;
  foodCost: number;
  activitiesCost: number;
  transportCost: number;
  totalEstimatedCost: number;
  currency: string;
};

const MAX_AMOUNT = 1000000;

function formatAmount(value) {
  const numericValue = Number(value || 0);

  if (!Number.isFinite(numericValue)) {
    return "0.00";
  }

  return new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(numericValue);
}

export function BudgetManagement() {
  const { activePlan } = usePlanContext();

  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [budgetForm, setBudgetForm] = useState({
    totalAmount: "",
    currency: "EUR",
  });
  const [editingBudgetId, setEditingBudgetId] = useState<string | null>(null);

  const [expenseForm, setExpenseForm] = useState({
    amount: "",
    category: "",
    date: "",
  });

  const [estimatedBudget, setEstimatedBudget] = useState<BudgetEstimateResponse | null>(null);
  const [estimateLoading, setEstimateLoading] = useState(false);
  const [estimateError, setEstimateError] = useState("");

  const loadBudgetData = async () => {
    if (!activePlan) {
      setBudgets([]);
      setExpenses([]);
      setEstimatedBudget(null);
      setError("");
      return;
    }

    setEstimatedBudget(null);
    setEstimateError("");

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
      console.error("Budget data loading error:", fetchError);
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

  const categories = useMemo<CategorySummary[]>(() => {
    const grouped = expenses.reduce<Record<string, CategorySummary>>((accumulator, expense) => {
      const category = expense.category || "Uncategorized";

      accumulator[category] = accumulator[category] || {
        name: category,
        spent: 0,
      };

      accumulator[category].spent += expense.amount || 0;

      return accumulator;
    }, {});

    return Object.values(grouped);
  }, [expenses]);

  const validateAmount = (value: string, label: string) => {
    const amount = Number(value);

    if (!value || Number.isNaN(amount)) {
      setError(`${label} amount is required.`);
      return null;
    }

    if (!Number.isFinite(amount)) {
      setError(`${label} amount is not valid.`);
      return null;
    }

    if (amount <= 0) {
      setError(`${label} amount must be greater than 0.`);
      return null;
    }

    if (amount > MAX_AMOUNT) {
      setError(`${label} amount cannot be greater than ${formatAmount(MAX_AMOUNT)}.`);
      return null;
    }

    return amount;
  };

  const resetBudgetForm = () => {
    setEditingBudgetId(null);
    setBudgetForm({
      totalAmount: "",
      currency: "EUR",
    });
  };

  const handleCreateBudget = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const totalAmount = validateAmount(budgetForm.totalAmount, "Budget");

    if (totalAmount === null || !activePlan) {
      return;
    }

    try {
      setError("");

      const payload = {
        totalAmount,
        planId: activePlan.id,
        currency: budgetForm.currency,
      };

      if (editingBudgetId) {
        await updateBudget(editingBudgetId, payload);
      } else {
        await createBudget(payload);
      }

      resetBudgetForm();
      await loadBudgetData();
    } catch (createError) {
      console.error("Create budget error:", createError);
      setError(getApiErrorMessage(createError, editingBudgetId ? "Unable to update budget." : "Unable to create budget."));
    }
  };

  const handleEditBudget = (budget: Budget) => {
    setEditingBudgetId(budget.id || null);
    setBudgetForm({
      totalAmount: String(budget.totalAmount ?? ""),
      currency: budget.currency || "EUR",
    });
    setError("");
  };

  const handleDeleteBudget = async (budgetId?: string) => {
    if (!budgetId) return;

    try {
      setError("");
      await deleteBudget(budgetId);

      if (editingBudgetId === budgetId) {
        resetBudgetForm();
      }

      await loadBudgetData();
    } catch (deleteError) {
      console.error("Delete budget error:", deleteError);
      setError(getApiErrorMessage(deleteError, "Unable to delete budget."));
    }
  };

  const handleCreateExpense = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const amount = validateAmount(expenseForm.amount, "Expense");

    if (amount === null || !activePlan) {
      return;
    }

    try {
      setError("");

      await createExpense({
        amount,
        planId: activePlan.id,
        category: expenseForm.category,
        date: expenseForm.date ? new Date(expenseForm.date).toISOString() : new Date().toISOString(),
      });

      setExpenseForm({ amount: "", category: "", date: "" });
      await loadBudgetData();
    } catch (createError) {
      setError(getApiErrorMessage(createError, "Unable to create expense."));
    }
  };

  const handleEstimateBudget = async () => {
    if (!activePlan) return;

    try {
      setEstimateLoading(true);
      setEstimateError("");

      const data = await estimateBudget(activePlan.id);
      setEstimatedBudget(data);
    } catch (estimateError) {
      console.error("Budget estimation error:", estimateError);
      setEstimateError(getApiErrorMessage(estimateError, "Unable to estimate budget."));
    } finally {
      setEstimateLoading(false);
    }
  };

  if (!activePlan) {
    return (
      <ModuleEmpty
        title="No active plan selected"
        description="Choose a plan to manage its budget and expenses."
      />
    );
  }

  if (loading) {
    return <ModuleLoading label="Loading budget data..." />;
  }

  return (
    <div className="max-w-7xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-center justify-between gap-4 mb-4">
          <div>
            <h2 className="text-xl font-medium flex items-center gap-2">
              <TrendingUp className="w-5 h-5" />
              Automatic Budget Estimate
            </h2>
            <p className="text-sm text-gray-600 mt-1">
              Estimate travel costs based on destination, trip duration and average destination prices.
            </p>
          </div>

          <button
            type="button"
            onClick={handleEstimateBudget}
            disabled={estimateLoading}
            className="px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {estimateLoading ? "Calculating..." : "Estimate Budget"}
          </button>
        </div>

        {estimateError && (
          <p className="text-sm text-red-600 mb-4">{estimateError}</p>
        )}

        {estimatedBudget && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Destination</p>
              <p className="text-lg font-medium">{estimatedBudget.destination}</p>
            </div>

            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Number of days</p>
              <p className="text-lg font-medium">{estimatedBudget.numberOfDays}</p>
            </div>

            <div className="border border-blue-300 bg-blue-50 rounded p-4">
              <p className="text-sm text-blue-700">Total estimated cost</p>
              <p className="text-2xl font-semibold text-blue-900">
                {estimatedBudget.totalEstimatedCost} {estimatedBudget.currency}
              </p>
            </div>

            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Accommodation</p>
              <p className="font-medium">
                {estimatedBudget.accommodationCost} {estimatedBudget.currency}
              </p>
            </div>

            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Food</p>
              <p className="font-medium">
                {estimatedBudget.foodCost} {estimatedBudget.currency}
              </p>
            </div>

            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Activities</p>
              <p className="font-medium">
                {estimatedBudget.activitiesCost} {estimatedBudget.currency}
              </p>
            </div>

            <div className="border border-gray-300 rounded p-4">
              <p className="text-sm text-gray-600">Transport</p>
              <p className="font-medium">
                {estimatedBudget.transportCost} {estimatedBudget.currency}
              </p>
            </div>
          </div>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border-2 border-red-300 text-red-700 rounded-lg p-4 mb-6">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
        <div className="bg-white border-2 border-gray-300 rounded-lg p-6 overflow-hidden min-w-0">
          <div className="flex items-center justify-between gap-2 mb-2">
            <p className="text-sm text-gray-600">Total Budget</p>
            <DollarSign className="w-5 h-5 text-gray-400 shrink-0" />
          </div>
          <p className="text-xl sm:text-2xl lg:text-3xl font-medium break-all max-w-full">
            {formatAmount(totals.totalBudget)}
          </p>
        </div>

        <div className="bg-white border-2 border-gray-300 rounded-lg p-6 overflow-hidden min-w-0">
          <div className="flex items-center justify-between gap-2 mb-2">
            <p className="text-sm text-gray-600">Spent</p>
            <TrendingUp className="w-5 h-5 text-red-500 shrink-0" />
          </div>
          <p className="text-xl sm:text-2xl lg:text-3xl font-medium text-red-600 break-all max-w-full">
            {formatAmount(totals.totalSpent)}
          </p>
        </div>

        <div className="bg-white border-2 border-gray-300 rounded-lg p-6 overflow-hidden min-w-0">
          <div className="flex items-center justify-between gap-2 mb-2">
            <p className="text-sm text-gray-600">Remaining</p>
            <DollarSign className="w-5 h-5 text-green-500 shrink-0" />
          </div>
          <p className="text-xl sm:text-2xl lg:text-3xl font-medium text-green-600 break-all max-w-full">
            {formatAmount(totals.remaining)}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <DollarSign className="w-5 h-5" />
              Saved Budgets
            </h2>

            {budgets.length === 0 ? (
              <ModuleEmpty
                title="No budgets yet"
                description="Create a budget to start planning expected trip costs."
              />
            ) : (
              <div className="space-y-3">
                {budgets.map((budget) => (
                  <div
                    key={budget.id}
                    className={`border rounded p-4 flex justify-between items-start gap-4 ${
                      editingBudgetId === budget.id ? "border-blue-400 bg-blue-50" : "border-gray-300"
                    }`}
                  >
                    <div>
                      <p className="font-medium">
                        {formatAmount(budget.totalAmount)} {budget.currency || "EUR"}
                      </p>
                      <p className="text-sm text-gray-600">Plan ID: {budget.planId}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <button
                        type="button"
                        onClick={() => handleEditBudget(budget)}
                        className="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        onClick={() => handleDeleteBudget(budget.id)}
                        className="px-3 py-2 border border-gray-300 rounded hover:bg-red-50"
                      >
                        <Trash2 className="w-4 h-4 text-red-600" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <PieChart className="w-5 h-5" />
              Expense Categories
            </h2>

            {categories.length === 0 ? (
              <ModuleEmpty
                title="No expenses yet"
                description="Start adding expenses to see category breakdown."
              />
            ) : (
              <div className="space-y-4">
                {categories.map((category) => (
                  <div key={category.name} className="border border-gray-300 rounded p-4 overflow-hidden min-w-0">
                    <div className="flex flex-wrap justify-between items-center gap-2">
                      <p className="font-medium break-words">{category.name}</p>
                      <p className="text-sm text-gray-600 break-all">
                        {formatAmount(category.spent)}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-6 overflow-hidden min-w-0">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <AlertCircle className="w-5 h-5 shrink-0" />
              Expenses
            </h2>

            {expenses.length === 0 ? (
              <ModuleEmpty
                title="No expenses recorded"
                description="Create expenses from the form to start tracking trip spending here."
              />
            ) : (
              <div className="space-y-3">
                {expenses.map((expense) => (
                  <div
                    key={expense.id}
                    className="border border-gray-300 rounded p-4 flex flex-wrap justify-between items-start gap-4 overflow-hidden min-w-0"
                  >
                    <div className="min-w-0">
                      <p className="font-medium break-words">
                        {expense.category || "Uncategorized"}
                      </p>
                      <p className="text-sm text-gray-600 break-words">
                        {new Date(expense.date).toLocaleString()}
                      </p>
                    </div>
                    <p className="font-medium break-all">
                      {formatAmount(expense.amount)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <form
            onSubmit={handleCreateBudget}
            className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3"
          >
            <h3 className="font-medium flex items-center gap-2">
              {editingBudgetId ? <Pencil className="w-4 h-4" /> : <Plus className="w-4 h-4" />}
              {editingBudgetId ? "Edit Budget" : "Add Budget"}
            </h3>

            <input
              required
              type="number"
              min="0"
              max={MAX_AMOUNT}
              step="0.01"
              value={budgetForm.totalAmount}
              onChange={(event) =>
                setBudgetForm({ ...budgetForm, totalAmount: event.target.value })
              }
              placeholder="Total amount"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />

            <input
              value={budgetForm.currency}
              onChange={(event) =>
                setBudgetForm({ ...budgetForm, currency: event.target.value })
              }
              placeholder="Currency"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />

            <div className="flex gap-2">
              <button className="flex-1 px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600">
                {editingBudgetId ? "Update Budget" : "Save Budget"}
              </button>
              {editingBudgetId ? (
                <button
                  type="button"
                  onClick={resetBudgetForm}
                  className="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50"
                >
                  <X className="w-4 h-4" />
                </button>
              ) : null}
            </div>
          </form>

          <form
            onSubmit={handleCreateExpense}
            className="bg-white border-2 border-gray-300 rounded-lg p-4 space-y-3"
          >
            <h3 className="font-medium flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Add Expense
            </h3>

            <input
              required
              type="number"
              min="0"
              max={MAX_AMOUNT}
              step="0.01"
              value={expenseForm.amount}
              onChange={(event) =>
                setExpenseForm({ ...expenseForm, amount: event.target.value })
              }
              placeholder="Amount"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />

            <input
              required
              value={expenseForm.category}
              onChange={(event) =>
                setExpenseForm({ ...expenseForm, category: event.target.value })
              }
              placeholder="Category"
              className="w-full px-3 py-2 border border-gray-300 rounded"
            />

            <input
              type="datetime-local"
              value={expenseForm.date}
              onChange={(event) =>
                setExpenseForm({ ...expenseForm, date: event.target.value })
              }
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
