package com.travelplanner.user_service.repository;

import com.travelplanner.user_service.model.PlanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanUserRepository extends JpaRepository<PlanUser, Integer> {
}
