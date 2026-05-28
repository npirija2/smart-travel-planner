package com.travelplanner.planning_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.travelplanner.planning_service.dto.RouteOptimizationResponseDTO;
import com.travelplanner.planning_service.dto.RouteStopDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final ActivityRepository activityRepository;
    private final TravelPlanRepository travelPlanRepository;

    public RouteOptimizationResponseDTO optimizeRoute(Long travelPlanId) {

        TravelPlan plan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));

        List<Activity> activities =
        activityRepository.findByDay_TravelPlan_Id(travelPlanId)
                .stream()
                .filter(a ->
                        a.getLocation() != null
                                && a.getLocation().getLatitude() != null
                                && a.getLocation().getLongitude() != null
                )
                .toList();

        if (activities.isEmpty()) {
            return RouteOptimizationResponseDTO.builder()
                    .travelPlanId(plan.getId())
                    .destinationName(plan.getDestination().getName())
                    .strategy("No activities available")
                    .totalDistanceScore(0.0)
                    .stops(List.of())
                    .build();
        }

        Map<String, List<Activity>> grouped =
                activities.stream()
                        .collect(Collectors.groupingBy(a ->
                                a.getDay().getDate() + "_" + a.getTimeslot()));

        List<RouteStopDTO> optimizedStops = new ArrayList<>();

        int order = 1;
        double totalDistance = 0.0;

        for (List<Activity> group : grouped.values()) {

            List<Activity> remaining = new ArrayList<>(group);

            Activity current = remaining.remove(0);

            optimizedStops.add(mapToStop(current, order++));

            while (!remaining.isEmpty()) {

                Activity nearest = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Activity candidate : remaining) {

                    double distance = calculateDistance(
                            current.getLocation(),
                            candidate.getLocation()
                    );

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = candidate;
                    }
                }

                totalDistance += nearestDistance;

                current = nearest;

                optimizedStops.add(mapToStop(current, order++));

                remaining.remove(current);
            }
        }

        return RouteOptimizationResponseDTO.builder()
                .travelPlanId(plan.getId())
                .destinationName(plan.getDestination().getName())
                .strategy("Sort by day, timeslot, and closest available coordinates")
                .totalDistanceScore(totalDistance)
                .stops(optimizedStops)
                .build();
    }

    private RouteStopDTO mapToStop(Activity activity, Integer order) {

        Location location = activity.getLocation();

        return RouteStopDTO.builder()
                .suggestedOrder(order)
                .activityId(activity.getId())
                .activityName(activity.getName())
                .dayId(activity.getDay().getId())
                .dayDate(activity.getDay().getDate())
                .timeslot(activity.getTimeslot())
                .locationId(location.getId())
                .locationName(location.getName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .build();
    }

    private double calculateDistance(Location a, Location b) {

        double lat1 = a.getLatitude();
        double lon1 = a.getLongitude();

        double lat2 = b.getLatitude();
        double lon2 = b.getLongitude();

        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double calc =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(calc), Math.sqrt(1 - calc));

        return earthRadius * c;
    }
}