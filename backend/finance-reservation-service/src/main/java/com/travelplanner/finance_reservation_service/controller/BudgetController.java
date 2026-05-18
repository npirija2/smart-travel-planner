package com.travelplanner.finance_reservation_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.finance_reservation_service.dto.BudgetEstimateResponse;
import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.service.BudgetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.ok(budgetService.getAllBudgets(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.ok(budgetService.getBudgetById(id, authHeader));
    }

    @GetMapping("/estimate/{planId}")
    public ResponseEntity<BudgetEstimateResponse> estimateBudget(
            @PathVariable Long planId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.ok(budgetService.estimateBudget(planId, authHeader));
    }

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByPlanId(
            @PathVariable Long planId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.ok(budgetService.getBudgetsByPlanId(planId, authHeader));
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @Valid @RequestBody BudgetRequestDTO dto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(dto, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequestDTO dto,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return ResponseEntity.ok(budgetService.updateBudget(id, dto, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        budgetService.deleteBudget(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}
