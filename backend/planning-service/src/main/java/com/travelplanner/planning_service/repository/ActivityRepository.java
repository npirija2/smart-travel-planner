package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByDayId(Long dayId);
    List<Activity> findByLocationId(Long locationId);
}