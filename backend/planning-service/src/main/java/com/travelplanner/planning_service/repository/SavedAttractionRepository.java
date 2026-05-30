package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.SavedAttraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedAttractionRepository extends JpaRepository<SavedAttraction, Long> {

    List<SavedAttraction> findByPlanId(Long planId);

    boolean existsByPlanIdAndLocationId(Long planId, Long locationId);

    void deleteByPlanIdAndLocationId(Long planId, Long locationId);
}