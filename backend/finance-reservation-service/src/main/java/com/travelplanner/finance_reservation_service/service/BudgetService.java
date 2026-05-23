package com.travelplanner.finance_reservation_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.shared.security.JwtValidator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMapper budgetMapper;
    private final JwtValidator jwtUtils; 

    public List<BudgetResponseDTO> getAllBudgets(String authHeader) {
        validateToken(authHeader); 
        return budgetRepository.findAll().stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }

    public List<BudgetResponseDTO> getBudgetsByPlanId(Long planId, String authHeader) {
        validateToken(authHeader); 
        return budgetRepository.findByPlanId(planId).stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }

    public BudgetResponseDTO getBudgetById(UUID id, String authHeader) {
        validateToken(authHeader); 
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));
        return budgetMapper.toResponseDTO(budget);
    }

    @Transactional
    public BudgetResponseDTO createBudget(BudgetRequestDTO dto, String authHeader) {
        validateToken(authHeader); 
        Budget budget = budgetMapper.toEntity(dto);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public BudgetResponseDTO updateBudget(UUID id, BudgetRequestDTO dto, String authHeader) {
        validateToken(authHeader); 
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));
        
        budgetMapper.updateEntityFromDTO(dto, budget);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteBudget(UUID id, String authHeader) {
        validateToken(authHeader); 
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget with ID " + id + " not found");
        }
        budgetRepository.deleteById(id);
    }

    @Transactional
    public Budget addExpenseToBudget(UUID budgetId, Expense expense, String authHeader) {
        validateToken(authHeader); 
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));

        budget.setTotalAmount(budget.getTotalAmount() - expense.getAmount());
        expenseRepository.save(expense);
        
        return budgetRepository.save(budget);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        jwtUtils.getClaims(token); 
    }
}
