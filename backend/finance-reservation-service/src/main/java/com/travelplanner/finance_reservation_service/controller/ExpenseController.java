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
import java.util.UUID; // Dodan import za UUID

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Manage plan expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @Operation(summary = "Get all expenses")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    // Promijenjeno u UUID
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get expenses by plan ID")
    // Promijenjeno u UUID
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPlanId(@PathVariable UUID planId) {
        return ResponseEntity.ok(expenseService.getExpensesByPlanId(planId));
    }

    @PostMapping
    @Operation(summary = "Create an expense")
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpenseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    // Promijenjeno u UUID
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}