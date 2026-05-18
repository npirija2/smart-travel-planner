package com.travelplanner.finance_reservation_service.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BudgetResponseDTO {
    private UUID id;
    private Double totalAmount;
    private Long planId;
    private String currency;
}
