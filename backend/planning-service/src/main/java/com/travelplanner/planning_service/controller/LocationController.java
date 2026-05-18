package com.travelplanner.planning_service.controller;

import com.travelplanner.planning_service.dto.LocationRequestDTO;
import com.travelplanner.planning_service.dto.LocationResponseDTO;
import com.travelplanner.planning_service.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<LocationResponseDTO>> getByDestinationId(@PathVariable Long destinationId) {
        return ResponseEntity.ok(locationService.getByDestinationId(destinationId));
    }

    @PostMapping
    public ResponseEntity<LocationResponseDTO> create(@Valid @RequestBody LocationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody LocationRequestDTO dto) {
        return ResponseEntity.ok(locationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
