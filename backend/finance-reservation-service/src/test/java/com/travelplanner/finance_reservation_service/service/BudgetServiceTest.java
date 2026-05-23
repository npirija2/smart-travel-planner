package com.travelplanner.finance_reservation_service.service;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.shared.security.JwtValidator;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private JwtValidator jwtUtils;

    private BudgetMapper budgetMapper;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetMapper = new BudgetMapper(); 
        budgetService = new BudgetService(budgetRepository, expenseRepository, budgetMapper, jwtUtils);
    }

    @Test
    void testGetBudgetById_Success() {
        UUID id = UUID.randomUUID();
        Budget budget = new Budget();
        budget.setId(id);
        budget.setTotalAmount(100.0);

        when(budgetRepository.findById(id)).thenReturn(Optional.of(budget));

        BudgetResponseDTO result = budgetService.getBudgetById(id, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(100.0, result.getTotalAmount());
        verify(budgetRepository, times(1)).findById(id);
    }
}
