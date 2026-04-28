package com.travelplanner.communication_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.communication_service.dto.VoteRequestDTO;
import com.travelplanner.communication_service.dto.VoteResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoteController.class)
class VoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Koristi @MockBean ako je tvoj Spring Boot stariji od 3.4
    private VoteService voteService;

    @Autowired
    private ObjectMapper objectMapper;

    private VoteResponseDTO responseDTO;
    private VoteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new VoteResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUserId(10);
        responseDTO.setActivityId(50);

        requestDTO = new VoteRequestDTO();
        requestDTO.setUserId(10);
        requestDTO.setActivityId(50);
    }

    @Test
    void shouldCreateVote() throws Exception {
        when(voteService.createVote(any(VoteRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/votes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.activityId").value(50));
    }

    @Test
    void shouldGetAllVotes() throws Exception {
        when(voteService.getAllVotes()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/votes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetVoteById() throws Exception {
        when(voteService.getVoteById(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/votes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenVoteNotFound() throws Exception {
        when(voteService.getVoteById(99)).thenThrow(new ResourceNotFoundException("Vote not found"));

        mockMvc.perform(get("/api/votes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetVotesByUserId() throws Exception {
        when(voteService.getVotesByUserId(10)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/votes/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(10));
    }

    @Test
    void shouldGetVotesByActivityId() throws Exception {
        when(voteService.getVotesByActivityId(50)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/votes/activity/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activityId").value(50));
    }

    @Test
    void shouldDeleteVote() throws Exception {
        doNothing().when(voteService).deleteVote(1);

        mockMvc.perform(delete("/api/votes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn400WhenDuplicateVote() throws Exception {
        // Simuliramo bacanje greške iz servisa ako glas već postoji
        when(voteService.createVote(any(VoteRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("User has already voted for this activity"));

        mockMvc.perform(post("/api/votes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
}