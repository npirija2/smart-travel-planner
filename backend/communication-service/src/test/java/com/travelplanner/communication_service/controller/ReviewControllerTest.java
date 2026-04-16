package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateReview() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setUserId(1);
        request.setActivityId(1);
        request.setRating(5);
        request.setComment("Great");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(reviewService).createReview(any(ReviewRequestDTO.class));
    }

    @Test
    void shouldReturnValidationErrorForInvalidReview() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO();
        request.setUserId(1);
        request.setActivityId(1);
        request.setRating(10);
        request.setComment("");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
        when(reviewService.getReviewById(999))
                .thenThrow(new ResourceNotFoundException("Review not found with id 999"));

        mockMvc.perform(get("/api/reviews/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteReview() throws Exception {
        doNothing().when(reviewService).deleteReview(1);

        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteReview(1);
    }
}