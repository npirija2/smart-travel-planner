package com.travelplanner.finance_reservation_service.messaging.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationFailedEvent {

    private Long planId;
    private Long planReservationId;
    private String reason;
}