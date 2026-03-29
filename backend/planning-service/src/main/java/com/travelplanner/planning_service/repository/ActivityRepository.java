package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}