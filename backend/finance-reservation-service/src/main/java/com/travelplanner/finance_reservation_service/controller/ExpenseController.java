package com.travelplanner.finance_reservation_service.controller;

import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; 

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Manage plan expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @Operation(summary = "Get all expenses")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses(
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(expenseService.getAllExpenses(authHeader));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(expenseService.getExpenseById(id, authHeader));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get expenses by plan ID")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPlanId(
            @PathVariable UUID planId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(expenseService.getExpensesByPlanId(planId, authHeader));
    }

    @PostMapping
    @Operation(summary = "Create an expense")
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(dto, authHeader));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        expenseService.deleteExpense(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}