package com.travelplanner.finance_reservation_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
public class ExpenseRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private String category;

    private LocalDateTime date;
}
