package com.travelplanner.planning_service.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.planning_service.dto.ActivityResponseDTO;
import com.travelplanner.planning_service.dto.AttractionRecommendationDTO;
import com.travelplanner.planning_service.dto.DailyLoadDTO;
import com.travelplanner.planning_service.dto.DayDetailResponseDTO;
import com.travelplanner.planning_service.dto.LocalRecommendationDTO;
import com.travelplanner.planning_service.dto.RouteOptimizationResponseDTO;
import com.travelplanner.planning_service.dto.RouteStopDTO;
import com.travelplanner.planning_service.dto.ScheduleLoadResponseDTO;
import com.travelplanner.planning_service.dto.WaitingTimeInsightDTO;
import com.travelplanner.planning_service.dto.WeatherForecastResponseDTO;
import com.travelplanner.planning_service.exception.BadRequestException;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.LocationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;
import com.travelplanner.shared.security.JwtValidator;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanExperienceService {

    private final TravelPlanRepository travelPlanRepository;
    private final DayRepository dayRepository;
    private final ActivityRepository activityRepository;
    private final LocationRepository locationRepository;
    private final JwtValidator jwtUtils;
    private final WeatherForecastService weatherForecastService;

    public List<DayDetailResponseDTO> getPlanDays(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        return dayRepository.findByTravelPlanId(plan.getId()).stream()
                .sorted(Comparator.comparing(Day::getDate))
                .map(this::mapDayDetail)
                .toList();
    }

    public RouteOptimizationResponseDTO optimizeRoute(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        List<Activity> activities = getPlanActivities(plan.getId()).stream()
                .filter(activity -> activity.getLocation() != null)
                .sorted(Comparator
                        .comparing((Activity activity) -> activity.getDay().getDate())
                        .thenComparing(activity -> timeslotRank(activity.getTimeslot()))
                        .thenComparing(activity -> safeDouble(activity.getLocation().getLatitude()))
                        .thenComparing(activity -> safeDouble(activity.getLocation().getLongitude())))
                .toList();

        List<RouteStopDTO> stops = buildRouteStops(activities);
        return RouteOptimizationResponseDTO.builder()
                .travelPlanId(plan.getId())
                .destinationName(plan.getDestination().getName())
                .strategy("Sort by day, timeslot, and closest available coordinates")
                .totalDistanceScore(calculateDistanceScore(stops))
                .stops(stops)
                .build();
    }

    public ScheduleLoadResponseDTO analyzeScheduleLoad(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        List<DailyLoadDTO> dailyLoads = dayRepository.findByTravelPlanId(plan.getId()).stream()
                .sorted(Comparator.comparing(Day::getDate))
                .map(this::mapDailyLoad)
                .toList();

        long intenseDays = dailyLoads.stream()
                .filter(load -> "High".equals(load.getIntensity()))
                .count();

        String summary = intenseDays == 0
                ? "The schedule looks balanced across the trip."
                : "Some days are overloaded and should be rebalanced before travel.";

        return ScheduleLoadResponseDTO.builder()
                .travelPlanId(plan.getId())
                .summary(summary)
                .dailyLoads(dailyLoads)
                .build();
    }

    public List<AttractionRecommendationDTO> getAttractionRecommendations(Long planId, String interest, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        String normalizedInterest = normalize(interest);
        Set<Long> usedLocationIds = getPlanActivities(plan.getId()).stream()
                .map(activity -> activity.getLocation().getId())
                .collect(java.util.stream.Collectors.toSet());

        return locationRepository.findByDestinationId(plan.getDestination().getId()).stream()
                .filter(location -> matchesInterest(location, normalizedInterest))
                .sorted(Comparator.comparing((Location location) -> usedLocationIds.contains(location.getId())))
                .limit(8)
                .map(location -> AttractionRecommendationDTO.builder()
                        .locationId(location.getId())
                        .name(location.getName())
                        .address(location.getAddress())
                        .type(location.getType())
                        .destinationName(plan.getDestination().getName())
                        .reason(buildAttractionReason(location, normalizedInterest))
                        .matchScore(calculateMatchScore(location, normalizedInterest, usedLocationIds.contains(location.getId())))
                        .build())
                .toList();
    }

    public List<WeatherForecastResponseDTO> getWeatherForecast(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        List<LocalDate> dates = dayRepository.findByTravelPlanId(plan.getId()).stream()
                .sorted(Comparator.comparing(Day::getDate))
                .map(Day::getDate)
                .toList();

        return weatherForecastService.getForecastForPlan(plan, dates);
    }

    public List<LocalRecommendationDTO> getLocalRecommendations(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        return locationRepository.findByDestinationId(plan.getDestination().getId()).stream()
                .sorted(Comparator
                        .comparing((Location location) -> localPriority(location.getType()))
                        .thenComparing(Location::getName))
                .limit(8)
                .map(location -> LocalRecommendationDTO.builder()
                        .locationId(location.getId())
                        .name(location.getName())
                        .address(location.getAddress())
                        .type(location.getType())
                        .destinationName(plan.getDestination().getName())
                        .context(buildLocalContext(location))
                        .bestTimeslot(bestTimeslotForLocation(location.getType()))
                        .build())
                .toList();
    }

    public List<WaitingTimeInsightDTO> getWaitingTimeInsights(Long planId, String authHeader) {
        TravelPlan plan = getAuthorizedPlan(planId, authHeader);
        return getPlanActivities(plan.getId()).stream()
                .sorted(Comparator
                        .comparing((Activity activity) -> activity.getDay().getDate())
                        .thenComparing(activity -> timeslotRank(activity.getTimeslot())))
                .map(activity -> {
                    String type = normalize(activity.getLocation().getType());
                    int expectedWait = switch (type) {
                        case "culture", "museum" -> 25;
                        case "nature", "park" -> 10;
                        case "food", "restaurant" -> 20;
                        case "entertainment" -> 35;
                        default -> 18;
                    } + timeslotPenalty(activity.getTimeslot());

                    return WaitingTimeInsightDTO.builder()
                            .activityId(activity.getId())
                            .activityName(activity.getName())
                            .locationName(activity.getLocation().getName())
                            .expectedWaitMinutes(expectedWait)
                            .suggestedWindow(bestVisitWindow(activity.getTimeslot()))
                            .advice(expectedWait > 30
                                    ? "Consider visiting earlier or keeping a backup activity ready."
                                    : "Current slot is reasonable for a smooth visit.")
                            .build();
                })
                .toList();
    }

    private TravelPlan getAuthorizedPlan(Long planId, String authHeader) {
        TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));

        Long userId = getUserIdFromToken(authHeader);
        String role = getUserRoleFromToken(authHeader);

        if (!plan.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new BadRequestException("You are not authorized to access this plan");
        }

        return plan;
    }

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtUtils.getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    private String getUserRoleFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return (String) jwtUtils.getClaims(token).get("role");
    }

    private List<Activity> getPlanActivities(Long planId) {
        return dayRepository.findByTravelPlanId(planId).stream()
                .flatMap(day -> activityRepository.findByDayId(day.getId()).stream())
                .toList();
    }

    private DayDetailResponseDTO mapDayDetail(Day day) {
        List<ActivityResponseDTO> activities = activityRepository.findByDayId(day.getId()).stream()
                .sorted(Comparator
                        .comparing((Activity activity) -> timeslotRank(activity.getTimeslot()))
                        .thenComparing(activity -> activity.getStartTime() == null ? java.time.LocalTime.MAX : activity.getStartTime()))
                .map(this::mapActivity)
                .toList();

        return DayDetailResponseDTO.builder()
                .id(day.getId())
                .date(day.getDate())
                .travelPlanId(day.getTravelPlan().getId())
                .activities(activities)
                .build();
    }

    private ActivityResponseDTO mapActivity(Activity activity) {
        return ActivityResponseDTO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .dayId(activity.getDay().getId())
                .dayDate(activity.getDay().getDate())
                .createdBy(activity.getCreatedBy())
                .locationId(activity.getLocation().getId())
                .locationName(activity.getLocation().getName())
                .timeslot(activity.getTimeslot())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .duration(activity.getDuration())
                .status(activity.getStatus())
                .build();
    }

    private List<RouteStopDTO> buildRouteStops(List<Activity> activities) {
        java.util.concurrent.atomic.AtomicInteger counter = new java.util.concurrent.atomic.AtomicInteger(1);
        return activities.stream()
                .map(activity -> RouteStopDTO.builder()
                        .suggestedOrder(counter.getAndIncrement())
                        .activityId(activity.getId())
                        .activityName(activity.getName())
                        .dayId(activity.getDay().getId())
                        .dayDate(activity.getDay().getDate())
                        .timeslot(activity.getTimeslot())
                        .locationId(activity.getLocation().getId())
                        .locationName(activity.getLocation().getName())
                        .latitude(activity.getLocation().getLatitude())
                        .longitude(activity.getLocation().getLongitude())
                        .address(activity.getLocation().getAddress())
                        .build())
                .toList();
    }

    private double calculateDistanceScore(List<RouteStopDTO> stops) {
        double score = 0.0;
        for (int index = 1; index < stops.size(); index++) {
            RouteStopDTO previous = stops.get(index - 1);
            RouteStopDTO current = stops.get(index);
            score += Math.abs(safeDouble(current.getLatitude()) - safeDouble(previous.getLatitude()));
            score += Math.abs(safeDouble(current.getLongitude()) - safeDouble(previous.getLongitude()));
        }
        return Math.round(score * 100.0) / 100.0;
    }

    private DailyLoadDTO mapDailyLoad(Day day) {
        List<Activity> activities = activityRepository.findByDayId(day.getId());
        int totalDuration = activities.stream()
                .map(activity -> activity.getDuration() == null ? 90 : activity.getDuration())
                .reduce(0, Integer::sum);

        String intensity;
        String warning = "";

        if (activities.size() >= 5 || totalDuration > 480) {
            intensity = "High";
            warning = "This day may feel too intense for most travelers.";
        } else if (activities.size() >= 3 || totalDuration > 270) {
            intensity = "Medium";
            warning = "Check transfer times and leave buffer for delays.";
        } else {
            intensity = "Low";
            warning = "Balanced day with room for spontaneous stops.";
        }

        return DailyLoadDTO.builder()
                .dayId(day.getId())
                .date(day.getDate())
                .activityCount(activities.size())
                .totalDurationMinutes(totalDuration)
                .intensity(intensity)
                .warning(warning)
                .build();
    }

    private boolean matchesInterest(Location location, String normalizedInterest) {
        if (normalizedInterest.isBlank()) {
            return true;
        }

        String type = normalize(location.getType());
        return type.contains(normalizedInterest)
                || ("culture".equals(normalizedInterest) && (type.contains("museum") || type.contains("historic")))
                || ("nature".equals(normalizedInterest) && (type.contains("park") || type.contains("outdoor")))
                || ("fun".equals(normalizedInterest) && (type.contains("entertainment") || type.contains("night")));
    }

    private String buildAttractionReason(Location location, String normalizedInterest) {
        if (normalizedInterest.isBlank()) {
            return "Strong general fit for this destination.";
        }
        return "Matches your interest in " + normalizedInterest + " through its " + location.getType() + " profile.";
    }

    private Integer calculateMatchScore(Location location, String normalizedInterest, boolean alreadyUsed) {
        int score = matchesInterest(location, normalizedInterest) ? 90 : 65;
        if (alreadyUsed) {
            score -= 20;
        }
        return score;
    }

    private int localPriority(String type) {
        String normalizedType = normalize(type);
        return switch (normalizedType) {
            case "restaurant", "food" -> 1;
            case "event", "entertainment" -> 2;
            case "culture", "museum" -> 3;
            default -> 4;
        };
    }

    private String buildLocalContext(Location location) {
        String type = normalize(location.getType());
        return switch (type) {
            case "restaurant", "food" -> "Useful for meal breaks close to your plan.";
            case "event", "entertainment" -> "Good option if you want evening energy nearby.";
            case "culture", "museum" -> "Strong stop for deeper local context.";
            default -> "Worth considering as a flexible nearby addition.";
        };
    }

    private String bestTimeslotForLocation(String type) {
        String normalizedType = normalize(type);
        return switch (normalizedType) {
            case "restaurant", "food" -> "Evening";
            case "event", "entertainment" -> "Evening";
            case "culture", "museum" -> "Morning";
            case "park", "nature" -> "Morning";
            default -> "Afternoon";
        };
    }

    private String bestVisitWindow(String timeslot) {
        String normalizedTimeslot = normalize(timeslot);
        return switch (normalizedTimeslot) {
            case "morning" -> "08:00 - 10:30";
            case "midday", "podne", "afternoon" -> "11:00 - 13:30";
            case "evening", "vecer" -> "17:00 - 18:30";
            default -> "Early opening hours";
        };
    }

    private int timeslotPenalty(String timeslot) {
        String normalizedTimeslot = normalize(timeslot);
        return switch (normalizedTimeslot) {
            case "midday", "podne" -> 10;
            case "evening", "vecer" -> 5;
            default -> 0;
        };
    }

    private int timeslotRank(String timeslot) {
        String normalizedTimeslot = normalize(timeslot);
        return switch (normalizedTimeslot) {
            case "morning", "jutro" -> 1;
            case "midday", "podne", "afternoon" -> 2;
            case "evening", "vecer" -> 3;
            default -> 4;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
