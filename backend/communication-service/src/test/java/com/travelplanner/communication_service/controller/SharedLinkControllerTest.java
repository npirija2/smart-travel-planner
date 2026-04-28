package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.SharedLinkRequestDTO;
import com.travelplanner.communication_service.dto.SharedLinkResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.SharedLinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SharedLinkController.class)
class SharedLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private SharedLinkService sharedLinkService;

    @Autowired
    private ObjectMapper objectMapper;

    private SharedLinkResponseDTO responseDTO;
    private SharedLinkRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new SharedLinkResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUrl("http://example.com/share");
        responseDTO.setPlanId(10);
        responseDTO.setType("PLAN_SHARE");

        requestDTO = new SharedLinkRequestDTO();
        requestDTO.setUrl("http://example.com/share");
        requestDTO.setPlanId(10);
        requestDTO.setType("PLAN_SHARE");
    }

    @Test
    void shouldCreateSharedLink() throws Exception {
        when(sharedLinkService.createSharedLink(any(SharedLinkRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/shared-links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value("http://example.com/share"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetAllSharedLinks() throws Exception {
        when(sharedLinkService.getAllSharedLinks()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/shared-links"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].url").value("http://example.com/share"));
    }

    @Test
    void shouldGetSharedLinkById() throws Exception {
        when(sharedLinkService.getSharedLinkById(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/shared-links/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenSharedLinkNotFound() throws Exception {
        when(sharedLinkService.getSharedLinkById(99)).thenThrow(new ResourceNotFoundException("Link not found"));

        mockMvc.perform(get("/api/shared-links/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetSharedLinksByPlanId() throws Exception {
        when(sharedLinkService.getSharedLinksByPlanId(10)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/shared-links/plan/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planId").value(10));
    }

    @Test
    void shouldUpdateSharedLink() throws Exception {
        when(sharedLinkService.updateSharedLink(eq(1), any(SharedLinkRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/shared-links/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://example.com/share"));
    }

    @Test
    void shouldDeleteSharedLink() throws Exception {
        doNothing().when(sharedLinkService).deleteSharedLink(1);

        mockMvc.perform(delete("/api/shared-links/1"))
                .andExpect(status().isNoContent());
    }
}