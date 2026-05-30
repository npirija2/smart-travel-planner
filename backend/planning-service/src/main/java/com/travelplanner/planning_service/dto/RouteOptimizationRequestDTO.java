package com.travelplanner.planning_service.dto;

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
public class RouteOptimizationRequestDTO {

    private Long travelPlanId;
    private Double startLatitude;
    private Double startLongitude;
}