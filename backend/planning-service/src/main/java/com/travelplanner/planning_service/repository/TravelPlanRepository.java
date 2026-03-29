package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
}