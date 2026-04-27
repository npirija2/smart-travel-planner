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
import java.util.List;
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
    void shouldCreateNotificationsBatch() {
        NotificationRequestDTO first = new NotificationRequestDTO();
        first.setMessage("First");
        first.setDate(LocalDateTime.of(2026, 4, 16, 14, 0));
        first.setUserId(1);
        first.setPlanId(1);
        first.setType("INFO");

        NotificationRequestDTO second = new NotificationRequestDTO();
        second.setMessage("Second");
        second.setDate(LocalDateTime.of(2026, 4, 16, 15, 0));
        second.setUserId(1);
        second.setPlanId(1);
        second.setType("WARNING");

        Notification savedFirst = Notification.builder()
                .id(1)
                .message("First")
                .date(LocalDateTime.of(2026, 4, 16, 14, 0))
                .userId(1)
                .planId(1)
                .type("INFO")
                .build();

        Notification savedSecond = Notification.builder()
                .id(2)
                .message("Second")
                .date(LocalDateTime.of(2026, 4, 16, 15, 0))
                .userId(1)
                .planId(1)
                .type("WARNING")
                .build();

        when(notificationRepository.saveAll(any())).thenReturn(List.of(savedFirst, savedSecond));

        var result = notificationService.createNotifications(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals("First", result.get(0).getMessage());
        assertEquals("Second", result.get(1).getMessage());
    }

    @Test
    void shouldThrowWhenNotificationBatchIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> notificationService.createNotifications(List.of()));
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.getNotificationById(999));
    }
}
