package com.travelplanner.planning_service.repository;

import com.travelplanner.planning_service.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}