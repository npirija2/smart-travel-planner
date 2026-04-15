package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
}