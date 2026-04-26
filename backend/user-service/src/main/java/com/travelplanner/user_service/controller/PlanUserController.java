package com.travelplanner.user_service.controller;

import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.service.PlanUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlanUserController {

    private final PlanUserService planUserService;

    @PostMapping("/api/users/{userId}/plan-memberships")
    public ResponseEntity<PlanUserResponseDTO> createPlanUser(
            @PathVariable Integer userId,
            @Valid @RequestBody PlanUserRequestDTO request) {
        return new ResponseEntity<>(planUserService.createPlanUser(userId, request), HttpStatus.CREATED);
    }

    @GetMapping("/api/users/{userId}/plan-memberships")
    public ResponseEntity<List<PlanUserResponseDTO>> getPlanUsersByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(planUserService.getPlanUsersByUserId(userId));
    }

    @GetMapping("/api/plan-memberships/{planUserId}")
    public ResponseEntity<PlanUserResponseDTO> getPlanUserById(@PathVariable Integer planUserId) {
        return ResponseEntity.ok(planUserService.getPlanUserById(planUserId));
    }

    @PutMapping("/api/plan-memberships/{planUserId}")
    public ResponseEntity<PlanUserResponseDTO> updatePlanUser(
            @PathVariable Integer planUserId,
            @Valid @RequestBody PlanUserRequestDTO request) {
        return ResponseEntity.ok(planUserService.updatePlanUser(planUserId, request));
    }

    @DeleteMapping("/api/plan-memberships/{planUserId}")
    public ResponseEntity<Void> deletePlanUser(@PathVariable Integer planUserId) {
        planUserService.deletePlanUser(planUserId);
        return ResponseEntity.noContent().build();
    }
}
