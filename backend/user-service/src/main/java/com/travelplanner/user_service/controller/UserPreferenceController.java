package com.travelplanner.user_service.controller;

import com.travelplanner.user_service.dto.UserPreferenceRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceResponseDTO;
import com.travelplanner.user_service.dto.UserPreferenceBatchRequestDTO;
import com.travelplanner.user_service.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @PostMapping("/api/users/{userId}/preferences")
    public ResponseEntity<UserPreferenceResponseDTO> createPreference(
            @PathVariable Integer userId,
            @Valid @RequestBody UserPreferenceRequestDTO request) {
        return new ResponseEntity<>(userPreferenceService.createPreference(userId, request), HttpStatus.CREATED);
    }

    @PostMapping("/api/users/{userId}/preferences/batch")
    public ResponseEntity<List<UserPreferenceResponseDTO>> createPreferences(
            @PathVariable Integer userId,
            @Valid @RequestBody UserPreferenceBatchRequestDTO request) {
        return new ResponseEntity<>(userPreferenceService.createPreferences(userId, request.getPreferences()), HttpStatus.CREATED);
    }

    @GetMapping("/api/users/{userId}/preferences")
    public ResponseEntity<List<UserPreferenceResponseDTO>> getPreferencesByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(userPreferenceService.getPreferencesByUserId(userId));
    }

    @GetMapping("/api/preferences/{preferenceId}")
    public ResponseEntity<UserPreferenceResponseDTO> getPreferenceById(@PathVariable Integer preferenceId) {
        return ResponseEntity.ok(userPreferenceService.getPreferenceById(preferenceId));
    }

    @PutMapping("/api/preferences/{preferenceId}")
    public ResponseEntity<UserPreferenceResponseDTO> updatePreference(
            @PathVariable Integer preferenceId,
            @Valid @RequestBody UserPreferenceRequestDTO request) {
        return ResponseEntity.ok(userPreferenceService.updatePreference(preferenceId, request));
    }

    @DeleteMapping("/api/preferences/{preferenceId}")
    public ResponseEntity<Void> deletePreference(@PathVariable Integer preferenceId) {
        userPreferenceService.deletePreference(preferenceId);
        return ResponseEntity.noContent().build();
    }
}
