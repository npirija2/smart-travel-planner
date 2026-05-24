package com.travelplanner.communication_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "planning-service")
public interface PlanningServiceClient {

    @GetMapping("/api/activities/{id}/exists")
    Boolean activityExists(
        @PathVariable("id") Long id
    );
}