package com.travelplanner.finance_reservation_service.mapper;

import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.model.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetResponseDTO toResponseDTO(Budget budget) {
        if (budget == null) return null;
        BudgetResponseDTO dto = new BudgetResponseDTO();
        dto.setId(budget.getId());
        dto.setTotalAmount(budget.getTotalAmount());
        dto.setPlanId(budget.getPlanId());
        dto.setCurrency(budget.getCurrency());
        return dto;
    }

    public Budget toEntity(BudgetRequestDTO dto) {
        if (dto == null) return null;
        return Budget.builder()
                .totalAmount(dto.getTotalAmount())
                .planId(dto.getPlanId())
                .currency(dto.getCurrency())
                .build();
    }

    public void updateEntityFromDTO(BudgetRequestDTO dto, Budget budget) {
        if (dto == null || budget == null) return;
        budget.setTotalAmount(dto.getTotalAmount());
        budget.setCurrency(dto.getCurrency());
        budget.setPlanId(dto.getPlanId());
    }
}