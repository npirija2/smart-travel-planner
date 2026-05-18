import { useState } from "react";
import { estimateBudget } from "../api/budgetApi";
import "../styles/BudgetEstimate.css";

function BudgetEstimate({ planId }) {
  const [budget, setBudget] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleEstimateBudget = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await estimateBudget(planId);
      setBudget(response.data);
    } catch (err) {
      console.error("Budget estimate error:", err);
      setError("Budget estimate could not be loaded.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="budget-estimate-card">
      <div className="budget-estimate-header">
        <div>
          <h2>Travel budget estimate</h2>
          <p>
            Estimate based on destination, trip duration and average daily prices.
          </p>
        </div>

        <button onClick={handleEstimateBudget} disabled={loading}>
          {loading ? "Calculating..." : "Estimate budget"}
        </button>
      </div>

      {error && <p className="budget-error">{error}</p>}

      {budget && (
        <div className="budget-content">
          <div className="budget-main-info">
            <div>
              <span>Destination</span>
              <strong>{budget.destination}</strong>
            </div>

            <div>
              <span>Number of days</span>
              <strong>{budget.numberOfDays}</strong>
            </div>
          </div>

          <div className="budget-grid">
            <div className="budget-item">
              <span>Accommodation</span>
              <strong>
                {budget.accommodationCost} {budget.currency}
              </strong>
            </div>

            <div className="budget-item">
              <span>Food</span>
              <strong>
                {budget.foodCost} {budget.currency}
              </strong>
            </div>

            <div className="budget-item">
              <span>Activities</span>
              <strong>
                {budget.activitiesCost} {budget.currency}
              </strong>
            </div>

            <div className="budget-item">
              <span>Transport</span>
              <strong>
                {budget.transportCost} {budget.currency}
              </strong>
            </div>
          </div>

          <div className="budget-total">
            <span>Total estimated cost</span>
            <strong>
              {budget.totalEstimatedCost} {budget.currency}
            </strong>
          </div>
        </div>
      )}
    </section>
  );
}

export default BudgetEstimate;