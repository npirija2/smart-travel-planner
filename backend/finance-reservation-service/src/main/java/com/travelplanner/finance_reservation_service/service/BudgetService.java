package com.travelplanner.finance_reservation_service.service;

import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID; 

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMapper budgetMapper;

    public List<BudgetResponseDTO> getAllBudgets() {
        return budgetRepository.findAll().stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }

  
    public List<BudgetResponseDTO> getBudgetsByPlanId(UUID planId) {
        return budgetRepository.findByPlanId(planId).stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }


    public BudgetResponseDTO getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));
        return budgetMapper.toResponseDTO(budget);
    }

    @Transactional
    public BudgetResponseDTO createBudget(BudgetRequestDTO dto) {
        Budget budget = budgetMapper.toEntity(dto);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public BudgetResponseDTO updateBudget(UUID id, BudgetRequestDTO dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));
        
    
        budgetMapper.updateEntityFromDTO(dto, budget);
        
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget with ID " + id + " not found");
        }
        budgetRepository.deleteById(id);
    }

    @Transactional
    public Budget addExpenseToBudget(UUID budgetId, Expense expense) {
        // 1. Poziv prvom repository-u
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));

        // 2. Logika ažuriranja
        budget.setTotalAmount(budget.getTotalAmount() - expense.getAmount());

        // 3. Poziv drugom repository-u
        expenseRepository.save(expense);
        
        return budgetRepository.save(budget);
    }
}
