package com.travelplanner.finance_reservation_service.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "saga_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long planId;

    private Long planReservationId;

    private Long userId;

    private String reservationType;

    private String itemName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private SagaReservationStatus status;

    private String failureReason;
}