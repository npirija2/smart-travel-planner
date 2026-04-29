package com.travelplanner.planning_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning_service.dto.DayRequestDTO;
import com.travelplanner.planning_service.dto.DayResponseDTO;
import com.travelplanner.planning_service.service.DayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DayController.class)
class DayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DayService dayService;

    @Autowired
    private ObjectMapper objectMapper;

    private DayRequestDTO requestDTO;
    private DayResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Pretpostavljam da DayRequestDTO ima polja kao što su npr. date ili planId
        // Koristim builder ako ga imaš, ako ne, koristi običan set
        requestDTO = new DayRequestDTO();
        // requestDTO.setPlanId(1L); 

        responseDTO = new DayResponseDTO();
        responseDTO.setId(1L);
        // responseDTO.setDayNumber(1);
    }

    @Test
    void shouldCreateDay() throws Exception {
        when(dayService.create(any(DayRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/days")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Provjera za 201 status
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldGetAllDays() throws Exception {
        when(dayService.getAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.id").value(1L));
    }
}