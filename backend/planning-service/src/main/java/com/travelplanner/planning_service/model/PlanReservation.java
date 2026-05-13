package com.travelplanner.planning_service.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long planId;

    private Long userId;

    private String reservationType;

    private String itemName;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal amount;

    private String currency;

    private Long financeReservationId;

    @Enumerated(EnumType.STRING)
    private PlanReservationStatus status;

    private String failureReason;
}