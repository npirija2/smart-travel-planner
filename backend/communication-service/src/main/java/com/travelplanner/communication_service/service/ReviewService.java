package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public ReviewResponseDTO createReview(ReviewRequestDTO requestDTO) {
    Review review = new Review();
    review.setUserId(requestDTO.getUserId());
    review.setActivityId(requestDTO.getActivityId());
    review.setRating(requestDTO.getRating());
    review.setComment(requestDTO.getComment());

    Review saved = reviewRepository.save(review);

    ReviewResponseDTO response = new ReviewResponseDTO();
    response.setId(saved.getId());
    response.setUserId(saved.getUserId());
    response.setActivityId(saved.getActivityId());
    response.setRating(saved.getRating());
    response.setComment(saved.getComment());

    return response;
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

    private ReviewResponseDTO mapToResponseDto(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setActivityId(review.getActivityId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        return dto;
    }

    public Object getReviewsByUserId(int userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReviewsByUserId'");
    }

    public Object getReviewsByActivityId(int activityId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReviewsByActivityId'");
    }
}
