package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.client.PlanningServiceClient;
import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.exception.ServiceUnavailableException;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PlanningServiceClient planningServiceClient;

    public ReviewService(ReviewRepository reviewRepository,
                         PlanningServiceClient planningServiceClient) {
        this.reviewRepository = reviewRepository;
        this.planningServiceClient = planningServiceClient;
    }

    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) {
        validateActivityExists(requestDTO.getActivityId());

        Review review = new Review();
        review.setUserId(requestDTO.getUserId());
        review.setActivityId(requestDTO.getActivityId());
        review.setRating(requestDTO.getRating());
        review.setComment(requestDTO.getComment());

        Review saved = reviewRepository.save(review);

        return mapToResponseDto(saved);
    }

    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReviewById(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        return mapToResponseDto(review);
    }

    public ReviewResponseDTO updateReview(int id, ReviewRequestDTO requestDto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        validateActivityExists(requestDto.getActivityId());

        review.setUserId(requestDto.getUserId());
        review.setActivityId(requestDto.getActivityId());
        review.setRating(requestDto.getRating());
        review.setComment(requestDto.getComment());

        Review updated = reviewRepository.save(review);

        return mapToResponseDto(updated);
    }

    public void deleteReview(int id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id " + id));

        reviewRepository.delete(review);
    }

    public List<ReviewResponseDTO> getReviewsByUserId(int userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByActivityId(int activityId) {
        return reviewRepository.findByActivityId(activityId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private void validateActivityExists(int activityId) {
        Boolean exists;

        try {
            exists = planningServiceClient.activityExists((long) activityId);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Planning service trenutno nije dostupan. Nije moguće provjeriti aktivnost.");
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