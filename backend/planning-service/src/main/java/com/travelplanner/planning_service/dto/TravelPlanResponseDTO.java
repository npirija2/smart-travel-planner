package com.travelplanner.planning_service.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPlanResponseDTO {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ownerId;
    private Long destinationId;
    private String destinationName;
    private String description;
    private String status;
}