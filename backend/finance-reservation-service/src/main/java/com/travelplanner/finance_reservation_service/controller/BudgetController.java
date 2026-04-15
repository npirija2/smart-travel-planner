package com.travelplanner.finance_reservation_service.controller;

import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; 

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Tag(name = "Budgets", description = "Manage plan budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get all budgets")
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Budget found"),
            @ApiResponse(responseCode = "404", description = "Budget not found")
    })

    public ResponseEntity<BudgetResponseDTO> getBudgetById(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get budgets by plan ID")

    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByPlanId(@PathVariable UUID planId) {
        return ResponseEntity.ok(budgetService.getBudgetsByPlanId(planId));
    }

    @PostMapping
    @Operation(summary = "Create a budget")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Budget created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<BudgetResponseDTO> createBudget(@Valid @RequestBody BudgetRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")

    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequestDTO dto) {
        return ResponseEntity.ok(budgetService.updateBudget(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<Void> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}