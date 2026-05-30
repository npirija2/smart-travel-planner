package com.travelplanner.planning_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.travelplanner.planning_service.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByDayId(Long dayId);
    List<Activity> findByLocationId(Long locationId);
    List<Activity> findByDay_TravelPlan_Id(Long travelPlanId);
}