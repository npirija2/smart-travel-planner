package com.travelplanner.planning_service.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.travelplanner.planning_service.dto.RouteOptimizationResponseDTO;
import com.travelplanner.planning_service.dto.RouteStopDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final ActivityRepository activityRepository;
    private final TravelPlanRepository travelPlanRepository;
    @Value("${ors.api.key}")
    private String orsApiKey;
    @PostConstruct
        public void testKey() {
        System.out.println("ORS KEY = " + orsApiKey);
        }
    private final RestTemplate restTemplate = new RestTemplate();
    public RouteOptimizationResponseDTO optimizeRoute(Long travelPlanId) {
        return optimizeRoute(
                travelPlanId,
                null,
                null
        );
        }
    public RouteOptimizationResponseDTO optimizeRoute(Long travelPlanId, Double startLatitude, Double startLongitude) {

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
                    .originalStops(List.of())
                    .optimizedStops(List.of())
                    .build();
        }

        List<RouteStopDTO> originalStops = activities.stream().map(activity -> mapToStop(activity, 0)).toList();
        Map<String, List<Activity>> grouped = activities.stream().collect(Collectors.groupingBy(a ->a.getDay().getDate() + "_" + a.getTimeslot()));
        List<RouteStopDTO> optimizedStops = new ArrayList<>();

        int order = 1;
        double totalDistance = 0.0;

        for (List<Activity> group : grouped.values()) {
        List<Activity> remaining = new ArrayList<>(group);

        double currentLat;
        double currentLng;

        if (startLatitude != null && startLongitude != null) {
                currentLat = startLatitude;
                currentLng = startLongitude;

        } else {
                Activity first = remaining.remove(0);

                currentLat = first.getLocation().getLatitude();
                currentLng = first.getLocation().getLongitude();

                optimizedStops.add(mapToStop(first, order++));
        }

        while (!remaining.isEmpty()) {
                Activity nearest = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Activity candidate : remaining) {
                    double distance = calculateDistance(currentLat, currentLng, candidate.getLocation().getLatitude(), candidate.getLocation().getLongitude());
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = candidate;
                    }
                }
                totalDistance += nearestDistance;

                currentLat = nearest.getLocation().getLatitude();
                currentLng = nearest.getLocation().getLongitude();

                optimizedStops.add(mapToStop(nearest, order++));

                remaining.remove(nearest);
            }
        }

        return RouteOptimizationResponseDTO.builder()
                .travelPlanId(plan.getId())
                .destinationName(plan.getDestination().getName())
                .strategy("Sort by day, timeslot, and closest available coordinates")
                .totalDistanceScore(totalDistance)
                .originalStops(originalStops)
                .optimizedStops(optimizedStops)
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
        private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double calc =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(calc),
                Math.sqrt(1 - calc)
        );

        return earthRadius * c;
        }
        public String getRouteGeometry(List<List<Double>> coordinates, String transportMode) {
                System.out.println("ORS KEY = [" + orsApiKey + "]");
                String profile;
                switch (transportMode) {
                        case "walking":
                        profile = "foot-walking";
                        break;
                        case "cycling":
                        profile = "cycling-regular";
                        break;
                        default:
                        profile = "driving-car";
                }
                String url =
                        "https://api.openrouteservice.org/v2/directions/"
                                + profile
                                + "/geojson";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", orsApiKey);

                Map<String, Object> body = new HashMap<>();
                body.put("coordinates", coordinates);

                HttpEntity<Map<String, Object>> request =
                        new HttpEntity<>(body, headers);

                return restTemplate.postForObject(
                        url,
                        request,
                        String.class
                );
                }
}