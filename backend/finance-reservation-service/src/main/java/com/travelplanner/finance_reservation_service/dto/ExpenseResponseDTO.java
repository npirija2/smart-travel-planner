package com.travelplanner.finance_reservation_service.dto;

import lombok.Data;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
public class ExpenseResponseDTO {
    private UUID id;
    private Double amount;
    private Long planId;
    private String category;
    private LocalDateTime date;
}
