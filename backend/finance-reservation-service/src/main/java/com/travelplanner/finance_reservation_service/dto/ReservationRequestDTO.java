package com.travelplanner.finance_reservation_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
public class ReservationRequestDTO {

    @NotBlank(message = "Type is required")
    private String type;

    private String details;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Double price;

    private String status;
}
