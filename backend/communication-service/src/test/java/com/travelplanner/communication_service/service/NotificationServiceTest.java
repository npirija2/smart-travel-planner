package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldCreateNotification() {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setMessage("Test");
        request.setDate(LocalDateTime.of(2026, 4, 16, 14, 0));
        request.setUserId(1);
        request.setPlanId(1);
        request.setType("INFO");

        Notification saved = new Notification();
        saved.setId(1);
        saved.setMessage("Test");
        saved.setDate(LocalDateTime.of(2026, 4, 16, 14, 0));
        saved.setUserId(1);
        saved.setPlanId(1);
        saved.setType("INFO");

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        var result = notificationService.createNotification(request);

        assertEquals(1, result.getId());
        assertEquals("Test", result.getMessage());
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(999));
    }
}