package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewResponseDTO responseDTO;
    private ReviewRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ReviewResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUserId(101);
        responseDTO.setActivityId(500);
        responseDTO.setRating(5);
        responseDTO.setComment("Sjajno!");

        requestDTO = new ReviewRequestDTO();
        requestDTO.setUserId(101);
        requestDTO.setActivityId(500);
        requestDTO.setRating(5);
        requestDTO.setComment("Sjajno!");
    }

    @Test
    void shouldCreateReview() throws Exception {
        when(reviewService.createReview(any(ReviewRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("Sjajno!"));
    }

    @Test
    void shouldGetAllReviews() throws Exception {
        when(reviewService.getAllReviews()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetReviewById() throws Exception {
        when(reviewService.getReviewById(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenReviewNotFound() throws Exception {
        when(reviewService.getReviewById(99)).thenThrow(new ResourceNotFoundException("Review not found"));

        mockMvc.perform(get("/api/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetReviewsByActivityId() throws Exception {
        when(reviewService.getReviewsByActivityId(500)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reviews/activity/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityId").value(500));
    }

    @Test
    void shouldUpdateReview() throws Exception {
        when(reviewService.updateReview(eq(1), any(ReviewRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteReview() throws Exception {
        doNothing().when(reviewService).deleteReview(1);

        mockMvc.perform(delete("/api/reviews/1"))
                .andExpect(status().isNoContent());
    }
}