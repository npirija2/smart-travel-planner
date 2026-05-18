package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByDestinationId(Long destinationId);
}
