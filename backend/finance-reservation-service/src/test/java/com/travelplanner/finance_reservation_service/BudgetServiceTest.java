package com.travelplanner.finance_reservation_service;

import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.service.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    // Umjesto @Mock, koristimo pravu klasu jer Mockito puca na Javi 23
    private BudgetMapper budgetMapper;

    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetMapper = new BudgetMapper(); // Prava instanca
        budgetService = new BudgetService(budgetRepository, budgetMapper);
    }

    @Test
    void testGetBudgetById_Success() {
        UUID id = UUID.randomUUID();
        Budget budget = new Budget();
        budget.setId(id);
        budget.setTotalAmount(100.0);

        when(budgetRepository.findById(id)).thenReturn(Optional.of(budget));

        BudgetResponseDTO result = budgetService.getBudgetById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(100.0, result.getTotalAmount());
        verify(budgetRepository, times(1)).findById(id);
    }
}