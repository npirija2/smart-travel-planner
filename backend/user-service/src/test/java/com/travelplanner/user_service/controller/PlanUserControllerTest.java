package com.travelplanner.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.exception.GlobalExceptionHandler;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.service.PlanUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
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
    void getPlanUserById_whenMissing_returns404() throws Exception {
        when(planUserService.getPlanUserById(9))
                .thenThrow(new ResourceNotFoundException("Plan korisnik sa ID-jem 9 nije pronađen"));

        mockMvc.perform(get("/api/plan-memberships/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.status").value(404));
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

    @Test
    void createPlanUser_withInvalidPayload_returns400() throws Exception {
        PlanUserRequestDTO request = new PlanUserRequestDTO();

        mockMvc.perform(post("/api/users/1/plan-memberships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
