package com.travelplanner.communication_service.controller;

import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.dto.NotificationResponseDTO;
import com.travelplanner.communication_service.dto.NotificationBatchRequestDTO;
import com.travelplanner.communication_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "Endpoints for managing notifications")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Create notification")
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> createNotification(
            @Valid @RequestBody NotificationRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO

        NotificationResponseDTO response = notificationService.createNotification(requestDTO, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create notifications in batch")
    @PostMapping("/batch")
    public ResponseEntity<List<NotificationResponseDTO>> createNotificationsBatch(
            @Valid @RequestBody NotificationBatchRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotifications(requestDTO.getNotifications(), authHeader));
    }

    @Operation(summary = "Get all notifications")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications(
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(notificationService.getAllNotifications(authHeader));
    }

    @Operation(summary = "Get notification by id")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(
            @PathVariable int id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(notificationService.getNotificationById(id, authHeader));
    }

    @Operation(summary = "Get notifications by user id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByUserId(
            @PathVariable int userId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId, authHeader));
    }

    @Operation(summary = "Get notifications by plan id")
    @GetMapping("/plan/{planId}")
    public ResponseEntity<List<NotificationResponseDTO>> getNotificationsByPlanId(
            @PathVariable int planId,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(notificationService.getNotificationsByPlanId(planId, authHeader));
    }

    @Operation(summary = "Update notification")
    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> updateNotification(
            @PathVariable int id,
            @Valid @RequestBody NotificationRequestDTO requestDTO,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        return ResponseEntity.ok(notificationService.updateNotification(id, requestDTO, authHeader));
    }

    @Operation(summary = "Delete notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable int id,
            @RequestHeader("Authorization") String authHeader) { // DODANO
        notificationService.deleteNotification(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}