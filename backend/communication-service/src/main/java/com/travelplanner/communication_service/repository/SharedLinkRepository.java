package com.travelplanner.communication_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.communication_service.model.SharedLink;

@Repository
public interface SharedLinkRepository extends JpaRepository<SharedLink, Integer> {
}