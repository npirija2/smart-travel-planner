package com.travelplanner.finance_reservation_service.service;

import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.ExpenseMapper;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID; // Dodan import za UUID

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public List<ExpenseResponseDTO> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(expenseMapper::toResponseDTO)
                .toList();
    }

    // Promijenjeno u UUID planId
    public List<ExpenseResponseDTO> getExpensesByPlanId(UUID planId) {
        return expenseRepository.findByPlanId(planId).stream()
                .map(expenseMapper::toResponseDTO)
                .toList();
    }

    // Promijenjeno u UUID id
    public ExpenseResponseDTO getExpenseById(UUID id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + id + " not found"));
        return expenseMapper.toResponseDTO(expense);
    }

    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        if (expense.getDate() == null) {
            expense.setDate(LocalDateTime.now());
        }
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponseDTO(saved);
    }

    // Promijenjeno u UUID id
    @Transactional
    public void deleteExpense(UUID id) {
        if (!expenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Expense with ID " + id + " not found");
        }
        expenseRepository.deleteById(id);
    }
}