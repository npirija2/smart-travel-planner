package com.travelplanner.planning_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.exception.GlobalExceptionHandler;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.service.TravelPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelPlanController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TravelPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TravelPlanService travelPlanService;

    @Test
    void getAll_shouldReturn200() throws Exception {
        TravelPlanResponseDTO dto = TravelPlanResponseDTO.builder()
                .id(1L)
                .name("Test plan")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 5))
                .ownerId(1L)
                .destinationId(2L)
                .destinationName("Rim")
                .description("Opis")
                .status("PLANNED")
                .build();

        given(travelPlanService.getAll()).willReturn(List.of(dto));

        mockMvc.perform(get("/api/travel-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test plan"));
    }

    @Test
    void getById_whenMissing_shouldReturn404() throws Exception {
        given(travelPlanService.getById(9999L))
                .willThrow(new ResourceNotFoundException("Travel plan with id 9999 not found"));

        mockMvc.perform(get("/api/travel-plans/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void create_whenValid_shouldReturn201() throws Exception {
        TravelPlanRequestDTO request = TravelPlanRequestDTO.builder()
                .name("Ljetovanje")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .ownerId(1L)
                .destinationId(2L)
                .description("Opis")
                .status("PLANNED")
                .build();

        TravelPlanResponseDTO response = TravelPlanResponseDTO.builder()
                .id(10L)
                .name("Ljetovanje")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .ownerId(1L)
                .destinationId(2L)
                .destinationName("Rim")
                .description("Opis")
                .status("PLANNED")
                .build();

        given(travelPlanService.create(any(TravelPlanRequestDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Ljetovanje"));
    }

    @Test
    void create_whenInvalid_shouldReturn400() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "startDate": "2026-08-01",
                  "endDate": "2026-08-05",
                  "destinationId": 2
                }
                """;

        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"));
    }

    @Test
    void delete_whenExists_shouldReturn204() throws Exception {
        doNothing().when(travelPlanService).delete(eq(1L));

        mockMvc.perform(delete("/api/travel-plans/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenMissing_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Travel plan with id 9999 not found"))
                .when(travelPlanService).delete(9999L);

        mockMvc.perform(delete("/api/travel-plans/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
