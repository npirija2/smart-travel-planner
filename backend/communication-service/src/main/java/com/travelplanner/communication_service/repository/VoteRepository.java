package com.travelplanner.communication_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.communication_service.model.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Integer> {
    List<Vote> findByActivityId(Integer activityId);
    List<Vote> findByUserId(Integer userId);
    boolean existsByUserIdAndActivityId(Integer userId, Integer activityId);
}