import { Share2, Link as LinkIcon, Download, Copy, CheckCircle, QrCode } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { getApiErrorMessage } from "../../api/errorUtils";
import { createSharedLink, getSharedLinksByPlan } from "../../api/sharedLinkService";
import { usePlanContext } from "../context/PlanContext";
import { ModuleEmpty, ModuleError, ModuleLoading } from "./ModuleState";
import React from "react";

export function TravelPlanSharing() {
  const { activePlan } = usePlanContext();
  const [links, setLinks] = useState([]);
  const [copied, setCopied] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const shareLink = useMemo(() => {
    if (!activePlan) return "";
    return `${window.location.origin}/plans/${activePlan.id}`;
  }, [activePlan]);

  const loadLinks = async () => {
    if (!activePlan) {
      setLinks([]);
      return;
    }

    try {
      setLoading(true);
      setError("");
      const response = await getSharedLinksByPlan(activePlan.id);
      setLinks(response);
    } catch (fetchError) {
      setError(getApiErrorMessage(fetchError, "Unable to load shared links."));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLinks();
  }, [activePlan?.id]);

  const handleCopyLink = async () => {
    await navigator.clipboard.writeText(shareLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleGenerateLink = async () => {
    await createSharedLink({
      url: shareLink,
      planId: activePlan.id,
      type: "VIEW",
    });
    await loadLinks();
  };

  if (!activePlan) return <ModuleEmpty title="No active plan selected" description="Choose a plan first to generate and manage share links." action={undefined} />;
  if (loading) return <ModuleLoading label="Loading sharing data..." />;
  if (error) return <ModuleError message={error} />;

  return (
    <div className="max-w-6xl mx-auto">
      <div className="bg-white border-2 border-gray-300 rounded-lg p-6 mb-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 border border-blue-400 rounded-lg flex items-center justify-center flex-shrink-0">
            <Share2 className="w-6 h-6 text-blue-600" />
          </div>
          <div className="flex-1">
            <h1 className="text-2xl font-medium mb-2">Share Travel Plan</h1>
            <p className="text-gray-600">Create and manage shareable links for your active travel plan.</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4">Quick Share Link</h2>
            <div className="bg-gray-50 border border-gray-300 rounded p-4 mb-4">
              <p className="text-sm text-gray-600 mb-2">Share URL for the active plan</p>
              <div className="flex gap-2">
                <input type="text" value={shareLink} readOnly className="flex-1 px-4 py-2 border border-gray-300 rounded bg-white font-mono text-sm" />
                <button onClick={handleCopyLink} className="px-4 py-2 bg-blue-500 text-white border-2 border-blue-600 rounded hover:bg-blue-600 flex items-center gap-2">
                  {copied ? <CheckCircle className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                  {copied ? "Copied!" : "Copy"}
                </button>
              </div>
            </div>
            <button onClick={handleGenerateLink} className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 text-sm flex items-center gap-2">
              <LinkIcon className="w-4 h-4" />
              Save Share Link
            </button>
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-6">
            <h2 className="text-xl font-medium mb-4">Saved Share Links</h2>
            {links.length === 0 ? (
              <ModuleEmpty title="No saved links" description="Generate a share link to keep it available for later use." action={undefined} />
            ) : (
              <div className="space-y-3">
                {links.map((link) => (
                  <div key={link.id} className="border border-gray-300 rounded p-4">
                    <p className="font-medium">{link.type}</p>
                    <p className="text-sm text-gray-600 break-all">{link.url}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white border-2 border-gray-300 rounded-lg p-4">
            <h3 className="font-medium mb-3 flex items-center gap-2">
              <QrCode className="w-5 h-5" />
              QR Code Preview
            </h3>
            <div className="border-2 border-gray-300 rounded bg-gray-50 h-48 flex items-center justify-center mb-3">
              <div className="w-32 h-32 bg-white border-2 border-gray-400 flex items-center justify-center">
                <QrCode className="w-8 h-8 text-gray-400" />
              </div>
            </div>
            <button className="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 text-sm">
              Download QR Placeholder
            </button>
          </div>

          <div className="bg-white border-2 border-gray-300 rounded-lg p-4">
            <h3 className="font-medium mb-3">Export Options</h3>
            <div className="space-y-2">
              <button className="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 text-sm flex items-center justify-center gap-2">
                <Download className="w-4 h-4" />
                Download current view
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
