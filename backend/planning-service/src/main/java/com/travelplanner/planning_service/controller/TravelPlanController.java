package com.travelplanner.planning_service.controller;

import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.service.TravelPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanController {

    private final TravelPlanService travelPlanService;

    @GetMapping
    public ResponseEntity<List<TravelPlanResponseDTO>> getAll() {
        return ResponseEntity.ok(travelPlanService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(travelPlanService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TravelPlanResponseDTO> create(@Valid @RequestBody TravelPlanRequestDTO dto) throws BadRequestException {
        return ResponseEntity.status(HttpStatus.CREATED).body(travelPlanService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPlanResponseDTO> update(@PathVariable Long id,
                                                        @Valid @RequestBody TravelPlanRequestDTO dto) throws BadRequestException {
        return ResponseEntity.ok(travelPlanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        travelPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}