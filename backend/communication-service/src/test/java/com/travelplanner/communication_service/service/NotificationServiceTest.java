package com.travelplanner.communication_service.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.dto.NotificationResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private NotificationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1);
        notification.setMessage("Test Message");
        notification.setUserId(10);
        notification.setPlanId(20);

        requestDTO = new NotificationRequestDTO();
        requestDTO.setMessage("Test Message");
        requestDTO.setUserId(10);
        requestDTO.setPlanId(20);
    }

    // --- TESTOVI ZA USPJEŠNE SCENARIJE ---

    @Test
    void shouldCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponseDTO response = notificationService.createNotification(requestDTO);

        assertNotNull(response);
        assertEquals("Test Message", response.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void shouldCreateMultipleNotifications() {
        List<NotificationRequestDTO> requests = List.of(requestDTO);
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(notification));

        List<NotificationResponseDTO> responses = notificationService.createNotifications(requests);

        assertEquals(1, responses.size());
        assertEquals("Test Message", responses.get(0).getMessage());
    }

    @Test
    void shouldReturnAllNotifications() {
        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        List<NotificationResponseDTO> result = notificationService.getAllNotifications();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetNotificationById() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));

        NotificationResponseDTO result = notificationService.getNotificationById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void shouldGetNotificationsByUserId() {
        when(notificationRepository.findByUserId(10)).thenReturn(List.of(notification));

        List<NotificationResponseDTO> result = notificationService.getNotificationsByUserId(10);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getUserId());
    }

    @Test
    void shouldUpdateNotificationSuccessfully() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponseDTO result = notificationService.updateNotification(1, requestDTO);

        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldDeleteNotificationSuccessfully() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        doNothing().when(notificationRepository).delete(notification);

        notificationService.deleteNotification(1);

        verify(notificationRepository, times(1)).delete(notification);
    }

    @Test
    void shouldThrowExceptionWhenNotificationNotFoundById() {
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(999);
        });
    }

    @Test
    void shouldThrowExceptionWhenCreatingEmptyNotificationList() {
        // Testiramo tvoju provjeru: if (requests == null || requests.isEmpty())
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotifications(Collections.emptyList());
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotifications(null);
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentNotification() {
        when(notificationRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.deleteNotification(999);
        });

        // Provjeravamo da se delete nikada ne pozove ako id ne postoji
        verify(notificationRepository, never()).delete(any());
    }
}