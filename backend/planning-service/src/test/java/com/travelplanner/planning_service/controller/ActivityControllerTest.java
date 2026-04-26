package com.travelplanner.planning_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning_service.dto.ActivityRequestDTO;
import com.travelplanner.planning_service.dto.ActivityResponseDTO;
import com.travelplanner.planning_service.exception.GlobalExceptionHandler;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.service.ActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActivityService activityService;

    @Test
    void create_whenValid_shouldReturn201() throws Exception {

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Posjeta Koloseumu")
                .description("Jutarnji obilazak")
                .dayId(1L)
                .createdBy(1L)
                .locationId(1L)
                .timeslot("MORNING")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .status("PLANNED")
                .build();

        ActivityResponseDTO response = ActivityResponseDTO.builder()
                .id(1L)
                .name("Posjeta Koloseumu")
                .description("Jutarnji obilazak")
                .dayId(1L)
                .dayDate(LocalDate.of(2026, 6, 17))
                .createdBy(1L)
                .locationId(1L)
                .locationName("Colosseum")
                .timeslot("MORNING")
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .status("PLANNED")
                .build();

        given(activityService.create(any(ActivityRequestDTO.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Posjeta Koloseumu"));
    }

    @Test
    void create_whenInvalid_shouldReturn400() throws Exception {

        String invalidJson = """
                {
                  "name": "",
                  "dayId": 1,
                  "locationId": 1
                }
                """;

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    @Test
    void create_whenDayNotFound_shouldReturn404() throws Exception {

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .dayId(999L)
                .locationId(1L)
                .build();

        given(activityService.create(any(ActivityRequestDTO.class)))
                .willThrow(new ResourceNotFoundException("Day not found"));

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
