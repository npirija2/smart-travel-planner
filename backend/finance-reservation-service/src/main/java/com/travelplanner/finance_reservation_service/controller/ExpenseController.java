package com.travelplanner.finance_reservation_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.service.ExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Manage plan expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @Operation(summary = "Get all expenses")
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses(
            @RequestHeader("Authorization") String authHeader) { 
        return ResponseEntity.ok(expenseService.getAllExpenses(authHeader));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) { 
        return ResponseEntity.ok(expenseService.getExpenseById(id, authHeader));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get expenses by plan ID")
    public ResponseEntity<List<ExpenseResponseDTO>> getExpensesByPlanId(
            @PathVariable Long planId,
            @RequestHeader("Authorization") String authHeader) { 
        return ResponseEntity.ok(expenseService.getExpensesByPlanId(planId, authHeader));
    }

    @PostMapping
    @Operation(summary = "Create an expense")
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) { 
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(dto, authHeader));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) { 
        expenseService.deleteExpense(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}
