package com.travelplanner.planning_service.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanReservationRequestedEvent {

    private Long planId;
    private Long planReservationId;
    private Long userId;

    private String reservationType;
    private String itemName;

    private LocalDate startDate;
    private LocalDate endDate;

    private BigDecimal amount;
    private String currency;

    private boolean simulateFinanceFailure;
    private boolean simulatePlanningFinalizationFailure;
}