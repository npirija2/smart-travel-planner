package com.travelplanner.finance_reservation_service.controller;

import com.travelplanner.finance_reservation_service.controller.BudgetController;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.service.BudgetService;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) 
public class BudgetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BudgetService budgetService;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetController budgetController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();
    }

    @Test
    void shouldReturnBudgetById() throws Exception {
        UUID id = UUID.randomUUID();
        BudgetResponseDTO responseDTO = new BudgetResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTotalAmount(500.0);

        when(budgetService.getBudgetById(id)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/budgets/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(500.0));
    }
}