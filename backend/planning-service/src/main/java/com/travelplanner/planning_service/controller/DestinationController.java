package com.travelplanner.planning_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.planning_service.dto.DestinationRequestDTO;
import com.travelplanner.planning_service.dto.DestinationResponseDTO;
import com.travelplanner.planning_service.service.DestinationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService service;

    @GetMapping
    public List<DestinationResponseDTO> getAll() {
        return service.getAll();
    }

    @PostMapping
    public ResponseEntity<DestinationResponseDTO> create(@RequestBody @Valid DestinationRequestDTO dto) {
        return ResponseEntity.status(201).body(service.create(dto));
    }
}