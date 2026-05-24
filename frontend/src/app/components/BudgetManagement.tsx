import { DollarSign, PieChart, Plus, TrendingUp, AlertCircle } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createBudget, getBudgetsByPlan } from "../../api/budgetService";
import { createExpense, getExpensesByPlan } from "../../api/expenseService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";

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

  const validateAmount = (value, label) => {
    const amount = Number(value);

    if (!value || Number.isNaN(amount)) {
      alert(`${label} amount is required.`);
      return null;
    }

    if (!Number.isFinite(amount)) {
      alert(`${label} amount is not valid.`);
      return null;
    }

    if (amount <= 0) {
      alert(`${label} amount must be greater than 0.`);
      return null;
    }

    if (amount > MAX_AMOUNT) {
      alert(`${label} amount cannot be greater than ${formatAmount(MAX_AMOUNT)}.`);
      return null;
    }

    return amount;
  };

  const handleCreateBudget = async (event) => {
    event.preventDefault();

    const totalAmount = validateAmount(budgetForm.totalAmount, "Budget");

    if (totalAmount === null) {
      return;
    }

    try {
      await createBudget({
        totalAmount,
        planId: activePlan.id,
        currency: budgetForm.currency,
      });

      setBudgetForm({ totalAmount: "", currency: budgetForm.currency });
      await loadBudgetData();
    } catch (createError) {
      setError(getApiErrorMessage(createError, "Unable to create budget."));
    }
  };

  const handleCreateExpense = async (event) => {
    event.preventDefault();

    const amount = validateAmount(expenseForm.amount, "Expense");

    if (amount === null) {
      return;
    }

    try {
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

  if (error) {
    return <ModuleError message={error} />;
  }

  return (
    <div className="max-w-7xl mx-auto">
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
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6 overflow-hidden min-w-0">
            <h2 className="text-xl font-medium mb-4 flex items-center gap-2">
              <PieChart className="w-5 h-5 shrink-0" />
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
              <Plus className="w-4 h-4" />
              Add Budget
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

            <button className="w-full px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600">
              Save Budget
            </button>
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