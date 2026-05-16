package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.travelplanner.communication_service.client.PlanningServiceClient;
import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.exception.ServiceUnavailableException;
import com.travelplanner.communication_service.exception.UnauthorizedException;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.ReviewRepository;
import com.travelplanner.communication_service.util.JwtUtils;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlanningServiceClient planningServiceClient;
    private final JwtUtils jwtUtils;

    public ReviewService(ReviewRepository reviewRepository,
                         PlanningServiceClient planningServiceClient,
                         JwtUtils jwtUtils) { 
        this.reviewRepository = reviewRepository;
        this.planningServiceClient = planningServiceClient;
        this.jwtUtils = jwtUtils;
    }

    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader);
        validateActivityExists(requestDTO.getActivityId(), authHeader); // Prosljeđujemo token dalje

        Review review = new Review();
        review.setUserId(requestDTO.getUserId());
        review.setActivityId(requestDTO.getActivityId());
        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());

        Review saved = reviewRepository.save(review);
        return mapToResponseDto(saved);
    }

    public List<ReviewResponseDTO> getAllReviews(String authHeader) {
        validateToken(authHeader);
        return reviewRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReviewById(int id, String authHeader) {
        validateToken(authHeader);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        return mapToResponseDto(review);
    }

    public ReviewResponseDTO updateReview(int id, ReviewRequestDTO requestDto, String authHeader) {
        validateToken(authHeader);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        validateActivityExists(requestDto.getActivityId(), authHeader);

        review.setUserId(requestDto.getUserId());
        review.setActivityId(requestDto.getActivityId());
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());

        Review updated = reviewRepository.save(review);
        return mapToResponseDto(updated);
    }

    public void deleteReview(int id, String authHeader) {
        validateToken(authHeader);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        reviewRepository.delete(review);
    }

    public List<ReviewResponseDTO> getReviewsByUserId(int userId, String authHeader) {
        validateToken(authHeader);
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByActivityId(int activityId, String authHeader) {
        validateToken(authHeader);
        return reviewRepository.findByActivityId(activityId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException(
                    "Invalid or missing Authorization header");
        }
        try {
            jwtUtils.getClaims(authHeader.substring(7));
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    private void validateActivityExists(int activityId, String authHeader) {
        Boolean exists;

        try {
            // TOKEN RELAY: Prosljeđujemo authHeader u Planning Service poziv
            exists = planningServiceClient.activityExists((long) activityId, authHeader);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Planning service trenutno nije dostupan.");
        }

        if (exists == null || !exists) {
            throw new ResourceNotFoundException("Activity not found with id " + activityId);
        }
    }

    private ReviewResponseDTO mapToResponseDto(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setActivityId(review.getActivityId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        return dto;
    }
}