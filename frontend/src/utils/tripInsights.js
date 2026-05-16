export function enumerateTripDates(startDate, endDate) {
  const dates = [];
  const current = new Date(startDate);
  const target = new Date(endDate);

  while (current <= target) {
    dates.push(current.toISOString().slice(0, 10));
    current.setDate(current.getDate() + 1);
  }

  return dates;
}

export function calculateWorkload(activities) {
  const totalDuration = activities.reduce((sum, activity) => sum + (activity.duration || 60), 0);
  const score = activities.length * 18 + totalDuration / 12;

  if (score >= 100) {
    return {
      level: 'High',
      score,
      message: 'This day is intense. Consider moving one activity to another day.',
    };
  }

  if (score >= 60) {
    return {
      level: 'Balanced',
      score,
      message: 'The day is full but still manageable with smart timing.',
    };
  }

  return {
    level: 'Light',
    score,
    message: 'There is room for more stops or longer breaks.',
  };
}

function minutesFromTime(value) {
  if (!value || !/^\d{2}:\d{2}/.test(value)) {
    return null;
  }

  const [hours, minutes] = value.split(':').map(Number);
  return hours * 60 + minutes;
}

function fallbackWindow(timeslot) {
  if (timeslot === 'MORNING') {
    return [8 * 60, 12 * 60];
  }

  if (timeslot === 'NOON') {
    return [12 * 60, 17 * 60];
  }

  return [17 * 60, 22 * 60];
}

function normalizeWindow(activityLike) {
  const start = minutesFromTime(activityLike.startTime);
  const end = minutesFromTime(activityLike.endTime);

  if (start !== null && end !== null && end > start) {
    return [start, end];
  }

  return fallbackWindow(activityLike.timeslot);
}

export function findScheduleConflict(activities, candidate, ignoredActivityId = null) {
  const [candidateStart, candidateEnd] = normalizeWindow(candidate);

  return (
    activities.find((activity) => {
      if (ignoredActivityId !== null && activity.id === ignoredActivityId) {
        return false;
      }

      const [activityStart, activityEnd] = normalizeWindow(activity);
      return Math.max(candidateStart, activityStart) < Math.min(candidateEnd, activityEnd);
    }) || null
  );
}

function distanceBetween(a, b) {
  if (
    typeof a?.latitude !== 'number' ||
    typeof a?.longitude !== 'number' ||
    typeof b?.latitude !== 'number' ||
    typeof b?.longitude !== 'number'
  ) {
    return Number.POSITIVE_INFINITY;
  }

  const dx = a.latitude - b.latitude;
  const dy = a.longitude - b.longitude;
  return Math.sqrt(dx * dx + dy * dy);
}

export function optimizeRoute(activities, locations) {
  if (activities.length <= 1) {
    return activities;
  }

  const locationMap = new Map(locations.map((location) => [location.id, location]));
  const pool = [...activities];
  const optimized = [pool.shift()];

  while (pool.length) {
    const current = optimized[optimized.length - 1];
    const currentLocation = locationMap.get(current.locationId);

    let bestIndex = 0;
    let bestDistance = Number.POSITIVE_INFINITY;

    pool.forEach((candidate, index) => {
      const distance = distanceBetween(currentLocation, locationMap.get(candidate.locationId));

      if (distance < bestDistance) {
        bestDistance = distance;
        bestIndex = index;
      }
    });

    optimized.push(pool.splice(bestIndex, 1)[0]);
  }

  return optimized;
}

export function calculateRouteMetrics(activities, locations) {
  if (activities.length <= 1) {
    return {
      distanceKm: 0,
      transferMinutes: 0,
      summary: 'No route optimization is needed yet.',
    };
  }

  const locationMap = new Map(locations.map((location) => [location.id, location]));
  let totalDistance = 0;

  for (let index = 1; index < activities.length; index += 1) {
    const previous = locationMap.get(activities[index - 1].locationId);
    const current = locationMap.get(activities[index].locationId);
    const legDistance = distanceBetween(previous, current);
    totalDistance += Number.isFinite(legDistance) ? legDistance * 111 : 2.4;
  }

  const distanceKm = Math.round(totalDistance * 10) / 10;
  const transferMinutes = Math.round(totalDistance * 5 + (activities.length - 1) * 8);

  return {
    distanceKm,
    transferMinutes,
    summary:
      distanceKm <= 3
        ? 'The route is compact and walk-friendly.'
        : distanceKm <= 8
          ? 'The route is balanced, but grouping nearby stops will save time.'
          : 'The route is spread out. Consider moving one stop to another day.',
  };
}

export function estimateWaitTime(activity) {
  const baseline =
    activity.timeslot === 'MORNING'
      ? 18
      : activity.timeslot === 'NOON'
        ? 34
        : activity.timeslot === 'EVENING'
          ? 24
          : 28;

  const durationFactor = Math.max(0, (activity.duration || 60) / 15 - 3);
  const typeFactor = /museum|landmark|gallery|viewpoint/i.test(activity.locationName || '')
    ? 12
    : /restaurant|cafe|market/i.test(activity.locationName || '')
      ? 6
      : 0;

  return Math.round(baseline + durationFactor + typeFactor);
}

export function buildAttractionRecommendations(locations, destinationId, preferences) {
  const preferenceTokens = preferences
    .map((preference) => `${preference.preferenceType} ${preference.preferenceValue}`.toLowerCase())
    .join(' ');

  return locations
    .filter((location) => location.destinationId === destinationId)
    .map((location) => {
      let score = 10;

      if (/museum|culture|historic|gallery/i.test(location.type) && /culture|history|museum/.test(preferenceTokens)) {
        score += 25;
      }

      if (/park|nature|trail|lake|beach/i.test(location.type) && /nature|outdoor|hiking/.test(preferenceTokens)) {
        score += 25;
      }

      if (/nightlife|concert|fun|event/i.test(location.type) && /fun|nightlife|music/.test(preferenceTokens)) {
        score += 25;
      }

      if (/restaurant|food|cafe/i.test(location.type) && /food|local/.test(preferenceTokens)) {
        score += 15;
      }

      const reason =
        score >= 35
          ? 'Strong match with saved interests.'
          : /museum|historic|gallery/i.test(location.type)
            ? 'Works well for culture-focused days.'
            : /park|nature|beach|trail/i.test(location.type)
              ? 'Fits outdoor and scenic itineraries.'
              : /restaurant|food|cafe/i.test(location.type)
                ? 'Pairs naturally with meal breaks or evening plans.'
                : 'Useful as a flexible filler stop.';

      return { ...location, score, reason };
    })
    .sort((a, b) => b.score - a.score)
    .slice(0, 5);
}

export function buildNearbyRecommendations(locations, destinationId) {
  return locations
    .filter((location) => location.destinationId === destinationId)
    .filter((location) => /restaurant|food|cafe|event|concert|market|bar/i.test(location.type))
    .map((location) => ({
      ...location,
      bestMoment: /concert|event|bar/i.test(location.type)
        ? 'Best in the evening'
        : /cafe|coffee/i.test(location.type)
          ? 'Best in the morning'
          : 'Best around noon or early evening',
    }))
    .slice(0, 6);
}

function fallbackForecast(destinationName, startDate) {
  const month = new Date(startDate).getMonth() + 1;

  if (month >= 6 && month <= 8) {
    return {
      source: 'Heuristic forecast',
      summary: `Expect warm and mostly dry weather in ${destinationName}.`,
      temperature: '26°C',
      advice: 'Outdoor activities, city walks, and viewpoints are a strong fit.',
    };
  }

  if (month >= 12 || month <= 2) {
    return {
      source: 'Heuristic forecast',
      summary: `Expect cooler conditions in ${destinationName}.`,
      temperature: '8°C',
      advice: 'Prioritize museums, indoor attractions, and reservation-based visits.',
    };
  }

  return {
    source: 'Heuristic forecast',
    summary: `Expect mixed conditions in ${destinationName}.`,
    temperature: '18°C',
    advice: 'Combine flexible indoor and outdoor activities in the itinerary.',
  };
}

export async function loadForecast(plan, locations) {
  const anchor = locations.find(
    (location) =>
      location.destinationId === plan.destinationId &&
      typeof location.latitude === 'number' &&
      typeof location.longitude === 'number',
  );

  if (!anchor) {
    return fallbackForecast(plan.destinationName, plan.startDate);
  }

  try {
    const query = new URLSearchParams({
      latitude: anchor.latitude,
      longitude: anchor.longitude,
      current: 'temperature_2m,weather_code',
      timezone: 'auto',
    });

    const response = await fetch(`https://api.open-meteo.com/v1/forecast?${query.toString()}`);

    if (!response.ok) {
      throw new Error('Forecast request failed');
    }

    const payload = await response.json();
    const temperature = Math.round(payload.current?.temperature_2m ?? 0);

    return {
      source: 'Open-Meteo',
      summary: `Live weather data for ${plan.destinationName}.`,
      temperature: `${temperature}°C`,
      advice:
        temperature >= 24
          ? 'Schedule outdoor sightseeing and keep indoor breaks for the hottest hours.'
          : temperature <= 10
            ? 'Move key visits indoors and keep transport buffers longer.'
            : 'Current conditions support a balanced day across indoor and outdoor stops.',
    };
  } catch {
    return fallbackForecast(plan.destinationName, plan.startDate);
  }
}

function destinationCostProfile(destinationName = '') {
  const normalized = destinationName.toLowerCase();

  if (/paris|london|new york|zurich|copenhagen|amsterdam|dubai/.test(normalized)) {
    return { lodging: 160, dining: 52, mobility: 22, activities: 30 };
  }

  if (/rome|barcelona|vienna|prague|istanbul|athens/.test(normalized)) {
    return { lodging: 118, dining: 38, mobility: 16, activities: 22 };
  }

  return { lodging: 92, dining: 28, mobility: 12, activities: 18 };
}

export function estimateTripBudget(plan, days, activities, reservations) {
  const profile = destinationCostProfile(plan?.destinationName);
  const dayCount = Math.max(days.length, 1);
  const bookedReservationTotal = reservations.reduce(
    (sum, reservation) => sum + Number(reservation.price || 0),
    0,
  );
  const activityProjection = activities.length * profile.activities;
  const diningProjection = dayCount * profile.dining;
  const mobilityProjection = Math.max(dayCount - 1, 1) * profile.mobility;
  const lodgingProjection =
    bookedReservationTotal > 0 ? 0 : Math.max(dayCount - 1, 1) * profile.lodging;
  const baseTotal =
    bookedReservationTotal +
    activityProjection +
    diningProjection +
    mobilityProjection +
    lodgingProjection;
  const contingency = Math.round(baseTotal * 0.12);
  const estimatedTotal = Math.round((baseTotal + contingency) * 100) / 100;

  return {
    bookedReservationTotal: Math.round(bookedReservationTotal * 100) / 100,
    lodgingProjection,
    activityProjection,
    diningProjection,
    mobilityProjection,
    contingency,
    estimatedTotal,
    perDay: Math.round((estimatedTotal / dayCount) * 100) / 100,
    summary:
      estimatedTotal / dayCount >= 220
        ? 'This trip behaves like a premium itinerary.'
        : estimatedTotal / dayCount >= 130
          ? 'This trip fits a balanced mid-range budget.'
          : 'This trip stays in a lighter budget range.',
  };
}

export function buildWeatherActivityRecommendations(forecast, attractions, nearbySuggestions) {
  if (!forecast) {
    return [];
  }

  const numericTemperature = Number.parseInt(`${forecast.temperature}`, 10);
  const recommendations = [];

  if (!Number.isNaN(numericTemperature) && numericTemperature >= 25) {
    recommendations.push('Schedule outdoor landmarks before noon and keep midday for indoor breaks.');
  }

  if (!Number.isNaN(numericTemperature) && numericTemperature <= 10) {
    recommendations.push('Shift key sightseeing toward museums, galleries, and reserved experiences.');
  }

  if (/dry|warm|balanced|outdoor/i.test(forecast.advice)) {
    const outdoorPick = attractions.find((item) => /park|beach|trail|viewpoint|nature/i.test(item.type));
    if (outdoorPick) {
      recommendations.push(`Prioritize ${outdoorPick.name} while outdoor conditions are favorable.`);
    }
  }

  const indoorPick = attractions.find((item) => /museum|gallery|historic|culture/i.test(item.type));
  if (indoorPick && recommendations.length < 3) {
    recommendations.push(`Keep ${indoorPick.name} as your fallback if conditions shift during the day.`);
  }

  const mealPick = nearbySuggestions.find((item) => /restaurant|cafe|food/i.test(item.type));
  if (mealPick) {
    recommendations.push(`Plan a flexible meal stop at ${mealPick.name} to absorb delays or queue spikes.`);
  }

  return recommendations.slice(0, 4);
}

export function buildWaitTimeInsights(activities) {
  return activities
    .map((activity) => {
      const waitMinutes = estimateWaitTime(activity);
      const bestWindow =
        waitMinutes >= 40
          ? 'Arrive early in the morning'
          : waitMinutes >= 28
            ? 'Visit outside peak lunch hours'
            : 'Current slot is efficient';

      return {
        id: activity.id,
        name: activity.name,
        locationName: activity.locationName,
        waitMinutes,
        bestWindow,
      };
    })
    .sort((left, right) => right.waitMinutes - left.waitMinutes)
    .slice(0, 6);
}

export function saveOfflineSnapshot(planId, snapshot) {
  localStorage.setItem(`offline-plan-${planId}`, JSON.stringify(snapshot));
}

export function loadOfflineSnapshot(planId) {
  const raw = localStorage.getItem(`offline-plan-${planId}`);
  return raw ? JSON.parse(raw) : null;
}

export function exportPlanSnapshot(plan, snapshot) {
  const printable = window.open('', '_blank', 'width=1100,height=900');

  if (!printable) {
    return;
  }

  printable.document.write(`
    <html>
      <head>
        <title>${plan.name} itinerary</title>
        <style>
          body { font-family: Arial, sans-serif; margin: 32px; color: #1c1d22; }
          h1, h2, h3 { margin-bottom: 8px; }
          .card { border: 1px solid #e5e7eb; border-radius: 16px; padding: 16px; margin-bottom: 16px; }
          .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
          ul { padding-left: 18px; }
        </style>
      </head>
      <body>
        <h1>${plan.name}</h1>
        <p>${plan.destinationName} • ${plan.startDate} → ${plan.endDate}</p>
        <div class="grid">
          <div class="card">
            <h2>Summary</h2>
            <p>${plan.description || 'No description provided.'}</p>
            <p>Status: ${plan.status}</p>
          </div>
          <div class="card">
            <h2>Workload</h2>
            <p>${snapshot.workload.level} (${Math.round(snapshot.workload.score)})</p>
            <p>${snapshot.workload.message}</p>
          </div>
        </div>
        ${snapshot.days
          .map(
            (day) => `
            <div class="card">
              <h3>${day.date}</h3>
              <ul>
                ${day.activities
                  .map(
                    (activity) => `
                      <li>
                        <strong>${activity.name}</strong> — ${activity.locationName || 'Unknown location'}
                        ${activity.timeslot ? ` • ${activity.timeslot}` : ''}
                        ${activity.startTime ? ` • ${activity.startTime}` : ''}
                      </li>
                    `,
                  )
                  .join('')}
              </ul>
            </div>
          `,
          )
          .join('')}
      </body>
    </html>
  `);
  printable.document.close();
  printable.focus();
  printable.print();
}

export function downloadPlanSnapshot(plan, snapshot) {
  const payload = {
    exportedAt: new Date().toISOString(),
    plan,
    snapshot,
  };
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
  const objectUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = `${plan.name.toLowerCase().replace(/\s+/g, '-')}-offline-plan.json`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(objectUrl);
}
