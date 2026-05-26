import { useState } from "react";
import { Download, FileText } from "lucide-react";
import jsPDF from "jspdf";
import { ModuleEmpty } from "./ModuleState";
import api from "../../api/api";
import { usePlanContext } from "../context/PlanContext";

export function OfflineAccess() {
  const { plans, activePlanId } = usePlanContext();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const activePlan = plans.find((plan: any) => plan.id === activePlanId);

  const downloadPlanPdf = async () => {
    if (!activePlanId) {
      setError("Please select a travel plan first.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await api.get(`/travel-plans/${activePlanId}`);
      const plan = response.data;

      const pdf = new jsPDF("p", "mm", "a4");

      let y = 20;

      pdf.setFontSize(18);
      pdf.text("Travel Plan", 15, y);

      y += 12;

      pdf.setFontSize(12);
      pdf.text(`Plan ID: ${plan.id ?? activePlanId}`, 15, y);
      y += 8;

      pdf.text(`Title: ${plan.name ?? plan.title ?? activePlan?.name ?? "N/A"}`, 15, y);
      y += 8;

      pdf.text(
        `Destination: ${plan.destinationName ?? activePlan?.destinationName ?? "N/A"}`,
        15,
        y
      );
      y += 8;

      pdf.text(`Start date: ${plan.startDate ?? activePlan?.startDate ?? "N/A"}`, 15, y);
      y += 8;

      pdf.text(`End date: ${plan.endDate ?? activePlan?.endDate ?? "N/A"}`, 15, y);
      y += 8;

      pdf.text(`Status: ${plan.status ?? activePlan?.status ?? "PLANNING"}`, 15, y);
      y += 12;

      if (plan.days && plan.days.length > 0) {
        pdf.setFontSize(14);
        pdf.text("Itinerary", 15, y);
        y += 10;

        plan.days.forEach((day: any, dayIndex: number) => {
          if (y > 270) {
            pdf.addPage();
            y = 20;
          }

          pdf.setFontSize(12);
          pdf.text(`Day ${day.dayNumber ?? dayIndex + 1}`, 15, y);
          y += 8;

          pdf.setFontSize(10);

          if (day.activities && day.activities.length > 0) {
            day.activities.forEach((activity: any) => {
              if (y > 270) {
                pdf.addPage();
                y = 20;
              }

              pdf.text(`Activity: ${activity.name ?? "N/A"}`, 20, y);
              y += 6;

              pdf.text(`Time: ${activity.time ?? "N/A"}`, 20, y);
              y += 6;

              pdf.text(`Location: ${activity.location ?? "N/A"}`, 20, y);
              y += 8;
            });
          } else {
            pdf.text("No activities available.", 20, y);
            y += 8;
          }

          y += 4;
        });
      } else {
        pdf.text("No daily itinerary available.", 15, y);
      }

      const titleForFile =
        plan.name ?? plan.title ?? activePlan?.name ?? `travel-plan-${activePlanId}`;

      const fileName = `${titleForFile
        .replace(/\s+/g, "-")
        .toLowerCase()}.pdf`;

      pdf.save(fileName);
    } catch (err) {
      setError(
        "Travel plan could not be downloaded. Check if backend is running and the selected plan exists."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center">
            <Download className="w-6 h-6 text-blue-600" />
          </div>

          <div className="flex-1">
            <h1 className="text-2xl font-medium mb-2">Offline Access</h1>

            <p className="text-gray-600 mb-4">
              Download the currently selected travel plan as a PDF for offline use.
            </p>

            <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
              <p className="text-sm text-gray-700 mb-3">
                Selected plan:{" "}
                <span className="font-semibold">
                  {activePlan?.name || "No plan selected"}
                </span>
              </p>

              <button
                onClick={downloadPlanPdf}
                disabled={loading || !activePlanId}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
              >
                <FileText className="w-4 h-4" />
                {loading ? "Downloading..." : "Download selected plan PDF"}
              </button>

              {error && <p className="text-red-600 text-sm mt-3">{error}</p>}
            </div>
          </div>
        </div>
      </div>

      <ModuleEmpty
        title="Download selected travel plan"
        description="Select a travel plan from the dashboard or top menu, then download it as a PDF file."
      />
    </div>
  );
}