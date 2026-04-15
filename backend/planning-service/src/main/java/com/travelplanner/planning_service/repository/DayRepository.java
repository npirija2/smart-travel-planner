package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DayRepository extends JpaRepository<Day, Long> {
    List<Day> findByTravelPlanId(Long travelPlanId);
}