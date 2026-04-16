package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void shouldGetReviewById() {
        Review review = new Review();
        review.setId(1);
        review.setUserId(1);
        review.setActivityId(1);
        review.setRating(5);
        review.setComment("Great");

        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        var result = reviewService.getReviewById(1);

        assertEquals(1, result.getId());
        assertEquals(5, result.getRating());
    }

    @Test
    void shouldThrowWhenReviewNotFound() {
        when(reviewRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(999));
    }

    @Test
    void shouldCreateReview() {
        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setUserId(1);
        request.setActivityId(1);
        request.setRating(4);
        request.setComment("Nice");

        Review saved = new Review();
        saved.setId(1);
        saved.setUserId(1);
        saved.setActivityId(1);
        saved.setRating(4);
        saved.setComment("Nice");

        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        var result = reviewService.createReview(request);

        assertEquals(1, result.getId());
        assertEquals("Nice", result.getComment());
    }
}