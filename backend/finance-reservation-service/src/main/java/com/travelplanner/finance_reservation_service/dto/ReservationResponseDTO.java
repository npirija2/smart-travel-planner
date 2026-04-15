package com.travelplanner.finance_reservation_service.dto;

import lombok.Data;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
public class ReservationResponseDTO {
    private UUID id;
    private String type;
    private String details;
    private UUID  planId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double price;
    private String status;
}
