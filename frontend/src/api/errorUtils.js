function inferServiceArea(url = "") {
  const normalizedUrl = String(url).toLowerCase();

  if (
    normalizedUrl.includes("/budgets") ||
    normalizedUrl.includes("/expenses") ||
    normalizedUrl.includes("/reservations") ||
    normalizedUrl.includes("/saga-reservations")
  ) {
    return "finance";
  }

  if (
    normalizedUrl.includes("/notifications") ||
    normalizedUrl.includes("/shared-links") ||
    normalizedUrl.includes("/votes") ||
    normalizedUrl.includes("/reviews")
  ) {
    return "communication";
  }

  if (
    normalizedUrl.includes("/travel-plans") ||
    normalizedUrl.includes("/days") ||
    normalizedUrl.includes("/activities") ||
    normalizedUrl.includes("/locations") ||
    normalizedUrl.includes("/destinations")
  ) {
    return "planning";
  }

  return "general";
}

function unavailableServiceMessage(url) {
  switch (inferServiceArea(url)) {
    case "finance":
      return "Finance features are temporarily unavailable. The rest of the app should still work normally.";
    case "communication":
      return "Collaboration and notification features are temporarily unavailable. Please try again shortly.";
    case "planning":
      return "Planning features are temporarily unavailable at the moment. Please try again shortly.";
    default:
      return "Some features are temporarily unavailable. Please try again shortly.";
  }
}

export function getApiErrorMessage(error, fallback = "Something went wrong.") {
  const payload = error?.response?.data;
  const status = error?.response?.status;
  const requestUrl = error?.config?.url;
  const errorCode = error?.code;

  if (
    status === 503 ||
    errorCode === "ECONNABORTED" ||
    errorCode === "ERR_NETWORK" ||
    (error?.request && !error?.response)
  ) {
    return unavailableServiceMessage(requestUrl);
  }

  if (typeof payload === "string" && payload.trim()) {
    return payload;
  }

  if (payload && typeof payload === "object") {
    if (typeof payload.message === "string" && payload.message.trim()) {
      return payload.message;
    }

    if (typeof payload.error === "string" && payload.error.trim()) {
      return payload.error;
    }
  }

  if (typeof error?.message === "string" && error.message.trim()) {
    return error.message;
  }

  return fallback;
}
