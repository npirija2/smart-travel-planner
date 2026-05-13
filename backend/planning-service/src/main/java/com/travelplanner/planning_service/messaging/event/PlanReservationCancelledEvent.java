package com.travelplanner.planning_service.messaging.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanReservationCancelledEvent {

    private Long planId;
    private Long planReservationId;
    private Long financeReservationId;
    private String reason;
}