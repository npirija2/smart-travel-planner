package com.travelplanner.finance_reservation_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelplanner.finance_reservation_service.model.SagaReservation;

public interface SagaReservationRepository extends JpaRepository<SagaReservation, Long> {

    Optional<SagaReservation> findByPlanReservationId(Long planReservationId);

    List<SagaReservation> findByPlanId(Long planId);
}