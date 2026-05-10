package com.travelplanner.communication_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.repository.NotificationRepository;
import com.travelplanner.communication_service.util.JwtUtils;

import io.jsonwebtoken.Claims;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        when(jwtUtils.getClaims("test-token")).thenReturn(mock(Claims.class));
    }

    @Test
    void shouldCreateFetchAndFilterNotificationsThroughFullStack() throws Exception {
        String request = """
                {
                  "message": "Flight changed",
                  "date": "2026-06-01T09:30:00",
                  "userId": 15,
                  "planId": 77,
                  "type": "INFO"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/notifications")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Flight changed"))
                .andExpect(jsonPath("$.userId").value(15))
                .andReturn();

        JsonNode createdNotification = objectMapper.readTree(createResult.getResponse().getContentAsString());
        int notificationId = createdNotification.get("id").asInt();

        assertThat(notificationRepository.count()).isEqualTo(1);
        assertThat(notificationRepository.findById(notificationId)).isPresent();

        mockMvc.perform(get("/api/notifications/{id}", notificationId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.planId").value(77));

        mockMvc.perform(get("/api/notifications/user/{userId}", 15)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(notificationId))
                .andExpect(jsonPath("$[0].message").value("Flight changed"));
    }

    @Test
    void shouldReturnValidationErrorAsJsonForInvalidNotificationRequest() throws Exception {
        String invalidRequest = """
                {
                  "message": "",
                  "userId": 15,
                  "planId": 77,
                  "type": ""
                }
                """;

        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}
