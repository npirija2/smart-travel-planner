package com.travelplanner.planning_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelplanner.planning_service.model.PlanReservation;

public interface PlanReservationRepository extends JpaRepository<PlanReservation, Long> {

    List<PlanReservation> findByPlanId(Long planId);
}