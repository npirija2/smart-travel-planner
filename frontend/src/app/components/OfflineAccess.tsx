import { Download } from "lucide-react";
import { ModuleEmpty } from "./ModuleState";

export function OfflineAccess() {
  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center">
            <Download className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h1 className="text-2xl font-medium mb-2">Offline Access</h1>
            <p className="text-gray-600">Download-friendly access to your plan will appear here.</p>
          </div>
        </div>
      </div>
      <ModuleEmpty
        title="Offline downloads coming soon"
        description="This space is reserved for downloadable trip files and offline-friendly travel access."
      />
    </div>
  );
}
