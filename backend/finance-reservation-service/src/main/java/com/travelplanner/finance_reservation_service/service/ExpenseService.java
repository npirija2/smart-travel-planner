package com.travelplanner.finance_reservation_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.ExpenseMapper;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.finance_reservation_service.util.JwtUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final JwtUtils jwtUtils; 

    public List<ExpenseResponseDTO> getAllExpenses(String authHeader) {
        validateToken(authHeader); 
        return expenseRepository.findAll().stream()
                .map(expenseMapper::toResponseDTO)
                .toList();
    }

    public List<ExpenseResponseDTO> getExpensesByPlanId(Long planId, String authHeader) {
        validateToken(authHeader); 
        return expenseRepository.findByPlanId(planId).stream()
                .map(expenseMapper::toResponseDTO)
                .toList();
    }

    public ExpenseResponseDTO getExpenseById(UUID id, String authHeader) {
        validateToken(authHeader); 
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + id + " not found"));
        return expenseMapper.toResponseDTO(expense);
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto, String authHeader) {
        validateToken(authHeader);
        Expense expense = expenseMapper.toEntity(dto);
        if (expense.getDate() == null) {
            expense.setDate(LocalDateTime.now());
        }
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteExpense(UUID id, String authHeader) {
        validateToken(authHeader);
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense with ID " + id + " not found");
        }
        expenseRepository.deleteById(id);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        jwtUtils.getClaims(token); 
    }
}
