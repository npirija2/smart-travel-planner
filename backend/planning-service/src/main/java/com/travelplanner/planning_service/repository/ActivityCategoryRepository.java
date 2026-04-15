package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.ActivityCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityCategoryRepository extends JpaRepository<ActivityCategory, Long> {
}