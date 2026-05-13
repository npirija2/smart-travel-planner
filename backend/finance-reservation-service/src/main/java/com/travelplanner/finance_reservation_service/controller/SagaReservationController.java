package com.travelplanner.finance_reservation_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.travelplanner.finance_reservation_service.model.SagaReservation;
import com.travelplanner.finance_reservation_service.repository.SagaReservationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/saga-reservations")
@RequiredArgsConstructor
public class SagaReservationController {

    private final SagaReservationRepository sagaReservationRepository;

    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<SagaReservation>> getByPlanId(@PathVariable Long planId) {
        return ResponseEntity.ok(sagaReservationRepository.findByPlanId(planId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SagaReservation> getById(@PathVariable Long id) {
        return sagaReservationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}