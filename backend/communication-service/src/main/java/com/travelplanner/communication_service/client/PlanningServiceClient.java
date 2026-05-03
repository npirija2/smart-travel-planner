package com.travelplanner.communication_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "planning-service")
public interface PlanningServiceClient {

    @GetMapping("/api/activities/exists/{id}")
    Boolean activityExists(
        @PathVariable("id") Long id, 
        @RequestHeader("Authorization") String authHeader
    );
}