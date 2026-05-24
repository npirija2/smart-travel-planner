package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.travelplanner.communication_service.client.PlanningServiceClient;
import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.exception.ServiceUnavailableException;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.ReviewRepository;
import com.travelplanner.shared.security.JwtValidator;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PlanningServiceClient planningServiceClient;

    @Mock
    private JwtValidator jwtUtils;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private ReviewRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        review = new Review();
        review.setId(1);
        review.setUserId(101);
        review.setActivityId(500);
        review.setRating(5);
        review.setComment("Odlična aktivnost!");

        requestDTO = new ReviewRequestDTO();
        requestDTO.setUserId(101);
        requestDTO.setActivityId(500);
        requestDTO.setRating(5);
        requestDTO.setComment("Odlična aktivnost!");
    }

    // --- USPJEŠNI SCENARIJI ---

    @Test
    void shouldCreateReviewSuccessfully() {
        // Mock-amo provjeru aktivnosti (vraća true)
        when(planningServiceClient.activityExists(500L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO response = reviewService.createReview(requestDTO, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(planningServiceClient).activityExists(500L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldReturnAllReviews() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<ReviewResponseDTO> result = reviewService.getAllReviews(AUTH_HEADER);

        assertEquals(1, result.size());
        assertEquals("Odlična aktivnost!", result.get(0).getComment());
    }

    @Test
    void shouldGetReviewById() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));

        ReviewResponseDTO response = reviewService.getReviewById(1, AUTH_HEADER);

        assertNotNull(response);
        assertEquals(1, response.getId());
    }

    @Test
    void shouldUpdateReviewSuccessfully() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        when(planningServiceClient.activityExists(anyLong())).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO updated = reviewService.updateReview(1, requestDTO, AUTH_HEADER);

        assertNotNull(updated);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void shouldDeleteReviewSuccessfully() {
        when(reviewRepository.findById(1)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(1, AUTH_HEADER);

        verify(reviewRepository).delete(review);
    }

    // --- SCENARIJI GREŠKE (ERROR CASES) ---

    @Test
    void shouldThrowExceptionWhenActivityDoesNotExist() {
        // Mock-amo da aktivnost NE postoji
        when(planningServiceClient.activityExists(500L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(requestDTO, AUTH_HEADER);
        });

        // Provjeravamo da se spremanje u bazu NIKADA nije dogodilo
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void shouldThrowServiceUnavailableWhenPlanningServiceFails() {
        // Simuliramo pad Planning servisa (baca Exception)
        when(planningServiceClient.activityExists(anyLong())).thenThrow(new RuntimeException("Down"));

        assertThrows(ServiceUnavailableException.class, () -> {
            reviewService.createReview(requestDTO, AUTH_HEADER);
        });
    }

    @Test
    void shouldThrowNotFoundWhenReviewDoesNotExist() {
        when(reviewRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReviewById(99, AUTH_HEADER);
        });
    }
}
