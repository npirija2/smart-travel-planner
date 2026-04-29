package com.travelplanner.finance_reservation_service.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.service.BudgetService;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetService budgetService;

    @Autowired
    private ObjectMapper objectMapper;

    private BudgetResponseDTO responseDTO;
    private BudgetRequestDTO requestDTO;
    private UUID budgetId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        budgetId = UUID.randomUUID();
        planId = UUID.randomUUID();

        responseDTO = new BudgetResponseDTO();
        responseDTO.setId(budgetId);
        responseDTO.setPlanId(planId);
        responseDTO.setTotalAmount(1000.0);

        requestDTO = new BudgetRequestDTO();
        requestDTO.setPlanId(planId);
        requestDTO.setTotalAmount(1000.0);
    }

    @Test
    void shouldGetAllBudgets() throws Exception {
        when(budgetService.getAllBudgets()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].totalAmount").value(1000.0));
    }

    @Test
    void shouldGetBudgetById() throws Exception {
        when(budgetService.getBudgetById(budgetId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/budgets/" + budgetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()));
    }

    @Test
    void shouldReturn404WhenBudgetNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(budgetService.getBudgetById(randomId))
                .thenThrow(new ResourceNotFoundException("Budget not found"));

        mockMvc.perform(get("/api/budgets/" + randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBudgetsByPlanId() throws Exception {
        when(budgetService.getBudgetsByPlanId(planId)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/budgets/plan/" + planId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0].planId").value(planId.toString()));
    }

    @Test
    void shouldCreateBudget() throws Exception {
        when(budgetService.createBudget(any(BudgetRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(1000.0));
    }

    @Test
    void shouldUpdateBudget() throws Exception {
        when(budgetService.updateBudget(eq(budgetId), any(BudgetRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/budgets/" + budgetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId.toString()));
    }

    @Test
    void shouldDeleteBudget() throws Exception {
        doNothing().when(budgetService).deleteBudget(budgetId);

        mockMvc.perform(delete("/api/budgets/" + budgetId))
                .andExpect(status().isNoContent());
    }
}