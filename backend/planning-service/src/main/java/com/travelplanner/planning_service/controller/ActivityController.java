package com.travelplanner.planning_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.planning_service.dto.ActivityRequestDTO;
import com.travelplanner.planning_service.dto.ActivityResponseDTO;
import com.travelplanner.planning_service.service.ActivityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<List<ActivityResponseDTO>> getAll() {
        return ResponseEntity.ok(activityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @GetMapping("/day/{dayId}")
    public ResponseEntity<List<ActivityResponseDTO>> getByDayId(@PathVariable Long dayId) {
        return ResponseEntity.ok(activityService.getByDayId(dayId));
    }

    @PostMapping
    public ResponseEntity<ActivityResponseDTO> create(@Valid @RequestBody ActivityRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDTO> update(@PathVariable Long id,
                                                      @Valid @RequestBody ActivityRequestDTO dto) {
        return ResponseEntity.ok(activityService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/day/{dayId}")
    public ResponseEntity<ActivityResponseDTO> addToDay(
            @PathVariable Long dayId,
            @RequestBody ActivityRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(activityService.addActivityToDay(dayId, dto));
    }


    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        boolean exists = activityService.existsById(id);
        return ResponseEntity.ok(exists);
    }
}