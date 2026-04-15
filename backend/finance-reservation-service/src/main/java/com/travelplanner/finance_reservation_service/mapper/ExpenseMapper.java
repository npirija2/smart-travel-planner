package com.travelplanner.finance_reservation_service.mapper;

import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.model.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponseDTO toResponseDTO(Expense expense) {
        if (expense == null) return null;
        ExpenseResponseDTO dto = new ExpenseResponseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setPlanId(expense.getPlanId());
        dto.setCategory(expense.getCategory());
        dto.setDate(expense.getDate());
        return dto;
    }

    public Expense toEntity(ExpenseRequestDTO dto) {
        if (dto == null) return null;
        return Expense.builder()
                .amount(dto.getAmount())
                .planId(dto.getPlanId())
                .category(dto.getCategory())
                .date(dto.getDate())
                .build();
    }
}