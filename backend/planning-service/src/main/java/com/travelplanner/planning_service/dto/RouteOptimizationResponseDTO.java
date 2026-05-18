package com.travelplanner.planning_service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteOptimizationResponseDTO {

    private Long travelPlanId;
    private String destinationName;
    private String strategy;
    private Double totalDistanceScore;
    private List<RouteStopDTO> stops;
}
