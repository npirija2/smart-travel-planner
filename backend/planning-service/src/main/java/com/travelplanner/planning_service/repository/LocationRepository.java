package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}