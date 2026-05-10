package com.travelplanner.communication_service.controller;

import com.travelplanner.communication_service.dto.ReviewRequestDTO;
import com.travelplanner.communication_service.dto.ReviewResponseDTO;
import com.travelplanner.communication_service.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO

        ReviewResponseDTO response = reviewService.createReview(requestDTO, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews(
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(reviewService.getAllReviews(authHeader));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> getReviewById(
            @PathVariable int id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(reviewService.getReviewById(id, authHeader));
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByActivityId(
            @PathVariable int activityId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(reviewService.getReviewsByActivityId(activityId, authHeader));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUserId(
            @PathVariable int userId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable int id,
            @Valid @RequestBody ReviewRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(reviewService.updateReview(id, dto, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable int id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        reviewService.deleteReview(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}