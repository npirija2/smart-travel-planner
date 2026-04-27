package com.travelplanner.planning_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelplanner.planning_service.model.TravelPlan;
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

List<TravelPlan> findByOwnerId(Long ownerId);
List<TravelPlan> findByStatus(String status);
}