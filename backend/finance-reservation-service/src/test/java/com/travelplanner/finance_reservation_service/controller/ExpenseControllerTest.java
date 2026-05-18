package com.travelplanner.finance_reservation_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.finance_reservation_service.dto.ExpenseRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ExpenseResponseDTO;
import com.travelplanner.finance_reservation_service.service.ExpenseService;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@ActiveProfiles("test")
class ExpenseControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseResponseDTO responseDTO;
    private ExpenseRequestDTO requestDTO;
    private UUID expenseId;
    private Long planId;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        planId = 202L;
        LocalDateTime now = LocalDateTime.now();

        responseDTO = new ExpenseResponseDTO();
        responseDTO.setId(expenseId);
        responseDTO.setPlanId(planId);
        responseDTO.setAmount(150.50);
        responseDTO.setCategory("Transport");
        responseDTO.setDate(now);

        requestDTO = new ExpenseRequestDTO();
        requestDTO.setPlanId(planId);
        requestDTO.setAmount(150.50);
        requestDTO.setCategory("Transport");
        requestDTO.setDate(now);
    }

    @Test
    void shouldCreateExpense() throws Exception {
        when(expenseService.createExpense(any(ExpenseRequestDTO.class), eq(AUTH_HEADER))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/expenses")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(150.50))
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    void shouldGetAllExpenses() throws Exception {
        when(expenseService.getAllExpenses(AUTH_HEADER)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/expenses").header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("Transport"));
    }

    @Test
    void shouldGetExpenseById() throws Exception {
        when(expenseService.getExpenseById(expenseId, AUTH_HEADER)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/expenses/" + expenseId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId.toString()));
    }

    @Test
    void shouldReturn404WhenExpenseNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(expenseService.getExpenseById(nonExistentId, AUTH_HEADER))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/expenses/" + nonExistentId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetExpensesByPlanId() throws Exception {
        when(expenseService.getExpensesByPlanId(planId, AUTH_HEADER)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/expenses/plan/" + planId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].planId").value(planId));
    }

    @Test
    void shouldDeleteExpense() throws Exception {
        doNothing().when(expenseService).deleteExpense(expenseId, AUTH_HEADER);

        mockMvc.perform(delete("/api/expenses/" + expenseId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
    }
}
