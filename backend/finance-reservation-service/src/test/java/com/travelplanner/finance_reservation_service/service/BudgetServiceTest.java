package com.travelplanner.finance_reservation_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.shared.security.JwtValidator;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private RestTemplate restTemplate;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetService = new BudgetService(
                budgetRepository,
                expenseRepository,
                budgetMapper,
                jwtValidator,
                restTemplate
        );

        when(jwtValidator.getClaims("test-token")).thenReturn(mock(Claims.class));
    }

    @Test
    void testGetBudgetByPlanIdSuccess() {
        Long planId = 3L;
        UUID id = UUID.randomUUID();

        Budget budget = new Budget();
        budget.setId(id);
        budget.setPlanId(planId);
        budget.setTotalAmount(100.0);

        BudgetResponseDTO responseDTO = new BudgetResponseDTO();
        responseDTO.setId(id);
        responseDTO.setTotalAmount(100.0);

        when(budgetRepository.findAllByPlanId(planId)).thenReturn(List.of(budget));
        when(budgetMapper.toResponseDTO(budget)).thenReturn(responseDTO);

        BudgetResponseDTO result = budgetService.getBudgetByPlanId(planId, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(100.0, result.getTotalAmount());
        verify(budgetRepository, times(1)).findAllByPlanId(planId);
    }
}
