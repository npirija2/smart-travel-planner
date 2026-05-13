package com.travelplanner.planning_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanReservationRequestDTO {

    @NotNull
    private Long userId;

    @NotBlank
    private String reservationType;

    @NotBlank
    private String itemName;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private boolean simulateFinanceFailure;

    private boolean simulatePlanningFinalizationFailure;
}