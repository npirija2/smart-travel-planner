package com.travelplanner.communication_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.communication_service.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByActivityId(Integer activityId);
    List<Review> findByUserId(Integer userId);
    List<Review> findByUserId(int userId);
    List<Review> findByActivityId(int activityId);
}