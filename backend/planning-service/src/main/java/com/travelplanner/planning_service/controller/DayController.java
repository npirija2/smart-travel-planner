package com.travelplanner.planning_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.planning_service.dto.DayRequestDTO;
import com.travelplanner.planning_service.dto.DayResponseDTO;
import com.travelplanner.planning_service.service.DayService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/days")
@RequiredArgsConstructor
public class DayController {

    private final DayService service;

    @PostMapping
    public ResponseEntity<DayResponseDTO> create(@RequestBody @Valid DayRequestDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }

    @GetMapping
    public List<DayResponseDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/plan/{travelPlanId}")
    public List<DayResponseDTO> getByTravelPlanId(@PathVariable Long travelPlanId) {
        return service.getByTravelPlanId(travelPlanId);
    }
}
