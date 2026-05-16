const STORAGE_KEY = 'smart-travel-finance-plan-map';

function readMap() {
  const raw = localStorage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : {};
}

function writeMap(map) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(map));
}

export function getFinancePlanId(planId) {
  const map = readMap();

  if (!map[planId]) {
    map[planId] = crypto.randomUUID();
    writeMap(map);
  }

  return map[planId];
}
