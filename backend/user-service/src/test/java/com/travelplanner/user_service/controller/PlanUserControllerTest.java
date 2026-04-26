package com.travelplanner.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.service.PlanUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlanUserController.class)
public class PlanUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanUserService planUserService;

    @Test
    void createPlanUser_returns201() throws Exception {
        PlanUserRequestDTO request = new PlanUserRequestDTO();
        request.setPlanId(42);
        request.setRole("OWNER");

        PlanUserResponseDTO response = new PlanUserResponseDTO();
        response.setId(9);
        response.setUserId(1);
        response.setPlanId(42);
        response.setRole("OWNER");

        when(planUserService.createPlanUser(eq(1), any(PlanUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/1/plan-memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.planId").value(42));
    }

    @Test
    void getPlanUsersByUser_returns200() throws Exception {
        PlanUserResponseDTO response = new PlanUserResponseDTO();
        response.setId(9);
        response.setUserId(1);
        response.setPlanId(42);
        response.setRole("OWNER");

        when(planUserService.getPlanUsersByUserId(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/1/plan-memberships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void updatePlanUser_returns200() throws Exception {
        PlanUserRequestDTO request = new PlanUserRequestDTO();
        request.setPlanId(55);
        request.setRole("MEMBER");

        PlanUserResponseDTO response = new PlanUserResponseDTO();
        response.setId(9);
        response.setUserId(1);
        response.setPlanId(55);
        response.setRole("MEMBER");

        when(planUserService.updatePlanUser(eq(9), any(PlanUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/plan-memberships/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void deletePlanUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/plan-memberships/9"))
                .andExpect(status().isNoContent());
    }
}
