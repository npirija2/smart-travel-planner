package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.NotificationRequestDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNotification() throws Exception {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setMessage("New notification");
        request.setDate(LocalDateTime.of(2026, 4, 16, 14, 0));
        request.setUserId(1);
        request.setPlanId(1);
        request.setType("INFO");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationService).createNotification(any(NotificationRequestDTO.class));
    }

    @Test
    void shouldReturnValidationErrorForInvalidNotification() throws Exception {
        NotificationRequestDTO request = new NotificationRequestDTO();
        request.setMessage("");
        request.setUserId(1);
        request.setPlanId(1);
        request.setType("INFO");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForMissingNotification() throws Exception {
        when(notificationService.getNotificationById(999))
                .thenThrow(new ResourceNotFoundException("Notification not found with id 999"));

        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound());
    }
}