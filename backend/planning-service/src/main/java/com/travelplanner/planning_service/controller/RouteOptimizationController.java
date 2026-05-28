package com.travelplanner.planning_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.planning_service.dto.RouteOptimizationResponseDTO;
import com.travelplanner.planning_service.service.RouteOptimizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    @GetMapping("/optimize/{travelPlanId}")
    public ResponseEntity<RouteOptimizationResponseDTO> optimize(
            @PathVariable Long travelPlanId
    ) {

        return ResponseEntity.ok(
                routeOptimizationService.optimizeRoute(travelPlanId)
        );
    }
}