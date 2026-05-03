package com.travelplanner.planning_service.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.service.TravelPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @Value("${server.port}")
    private String port;

    // Glavna metoda za dobavljanje planova (filtrira po korisniku iz tokena)
    @GetMapping
    public ResponseEntity<List<TravelPlanResponseDTO>> getAll(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.getAll(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> getById(@PathVariable Long id, 
                                                         @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.getById(id, authHeader));
    }

    @PostMapping
    public ResponseEntity<TravelPlanResponseDTO> create(@Valid @RequestBody TravelPlanRequestDTO dto,
                                                        @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(travelPlanService.create(dto, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> update(@PathVariable Long id,
                                                        @Valid @RequestBody TravelPlanRequestDTO dto,
                                                        @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(travelPlanService.update(id, dto, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        travelPlanService.delete(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/test")
    public String test() {
        return "Response from port: " + port;
    }
}