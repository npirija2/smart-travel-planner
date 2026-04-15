package com.travelplanner.finance_reservation_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.UUID;


@Data
public class BudgetRequestDTO {
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;

    @NotNull(message = "Plan ID is required")
    private UUID planId; // Promijenjeno u UUID

    private String currency;
}