package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.NotificationBatchRequestDTO;
import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.dto.NotificationResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationResponseDTO validResponse;
    private NotificationRequestDTO validRequest;

    @BeforeEach
        void setUp() {
            LocalDateTime now = LocalDateTime.now();

            validResponse = new NotificationResponseDTO();
            validResponse.setId(1);
            validResponse.setMessage("New notification");
            validResponse.setUserId(10);
            validResponse.setPlanId(5);
            validResponse.setType("INFO");
            validResponse.setDate(now); 

            validRequest = new NotificationRequestDTO();
            validRequest.setMessage("New notification");
            validRequest.setUserId(10);
            validRequest.setPlanId(5);
            validRequest.setType("INFO");
            validRequest.setDate(now);
        }

    @Test
    void shouldCreateNotification() throws Exception {
        when(notificationService.createNotification(any(NotificationRequestDTO.class), eq(AUTH_HEADER))).thenReturn(validResponse);

        mockMvc.perform(post("/api/notifications")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("New notification"));
    }

    @Test
    void shouldCreateNotificationsBatch() throws Exception {
        NotificationBatchRequestDTO batchRequest = new NotificationBatchRequestDTO();
        batchRequest.setNotifications(List.of(validRequest));

        when(notificationService.createNotifications(any(), eq(AUTH_HEADER))).thenReturn(List.of(validResponse));

        mockMvc.perform(post("/api/notifications/batch")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].message").value("New notification"));
    }

    @Test
    void shouldGetAllNotifications() throws Exception {
        when(notificationService.getAllNotifications(AUTH_HEADER)).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/notifications").header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetNotificationById() throws Exception {
        when(notificationService.getNotificationById(1, AUTH_HEADER)).thenReturn(validResponse);

        mockMvc.perform(get("/api/notifications/1").header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenNotificationNotFound() throws Exception {
        when(notificationService.getNotificationById(99, AUTH_HEADER)).thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/notifications/99").header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetNotificationsByUserId() throws Exception {
        when(notificationService.getNotificationsByUserId(10, AUTH_HEADER)).thenReturn(List.of(validResponse));

        mockMvc.perform(get("/api/notifications/user/10").header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10));
    }

    @Test
    void shouldUpdateNotification() throws Exception {
        when(notificationService.updateNotification(eq(1), any(NotificationRequestDTO.class), eq(AUTH_HEADER))).thenReturn(validResponse);

        mockMvc.perform(put("/api/notifications/1")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        mockMvc.perform(delete("/api/notifications/1").header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
    }
}
