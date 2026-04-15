package com.travelplanner.planning_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.planning_service.dto.ActivityCategoryRequestDTO;
import com.travelplanner.planning_service.service.ActivityCategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activity-categories")
@RequiredArgsConstructor
public class ActivityCategoryController {

    private final ActivityCategoryService service;

    @PostMapping
    public ResponseEntity<Void> assign(@RequestBody ActivityCategoryRequestDTO dto) {
        service.assign(dto);
        return ResponseEntity.status(201).build();
    }
}