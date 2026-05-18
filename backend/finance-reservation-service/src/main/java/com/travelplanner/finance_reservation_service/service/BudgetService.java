package com.travelplanner.finance_reservation_service.service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.travelplanner.finance_reservation_service.dto.BudgetEstimateResponse;
import com.travelplanner.finance_reservation_service.dto.BudgetRequestDTO;
import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.dto.TravelPlanResponse;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.BudgetMapper;
import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.shared.security.JwtValidator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMapper budgetMapper;
    private final JwtValidator jwtValidator;
    private final RestTemplate restTemplate;

    public List<BudgetResponseDTO> getAllBudgets(String authHeader) {
        validateToken(authHeader);
        return budgetRepository.findAll().stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }

    public BudgetResponseDTO getBudgetById(UUID id, String authHeader) {
        validateToken(authHeader);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));

        return budgetMapper.toResponseDTO(budget);
    }

    public List<BudgetResponseDTO> getBudgetsByPlanId(Long planId, String authHeader) {
        validateToken(authHeader);
        return budgetRepository.findAllByPlanId(planId).stream()
                .map(budgetMapper::toResponseDTO)
                .toList();
    }

    public BudgetResponseDTO getBudgetByPlanId(Long planId, String authHeader) {
        validateToken(authHeader);

        List<Budget> budgets = budgetRepository.findAllByPlanId(planId);
        if (budgets.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Budget for plan with ID " + planId + " not found"
            );
        }

        return budgetMapper.toResponseDTO(budgets.get(0));
    }
    @Transactional
    public BudgetResponseDTO createBudget(BudgetRequestDTO dto, String authHeader) {
        validateToken(authHeader);
        Budget budget = budgetMapper.toEntity(dto);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public BudgetResponseDTO updateBudget(UUID id, BudgetRequestDTO dto, String authHeader) {
        validateToken(authHeader);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget with ID " + id + " not found"));

        budgetMapper.updateEntityFromDTO(dto, budget);
        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteBudget(UUID id, String authHeader) {
        validateToken(authHeader);
        if (!budgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Budget with ID " + id + " not found");
        }
        budgetRepository.deleteById(id);
    }

    @Transactional
    public Budget addExpenseToBudget(UUID budgetId, Expense expense, String authHeader) {
        validateToken(authHeader);
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));

        budget.setTotalAmount(budget.getTotalAmount() - expense.getAmount());
        expenseRepository.save(expense);

        return budgetRepository.save(budget);
    }

    public BudgetEstimateResponse estimateBudget(Long planId, String authHeader) {
        validateToken(authHeader);

        TravelPlanResponse plan = getTravelPlanFromPlanningService(planId, authHeader);

        if (plan == null) {
            throw new ResourceNotFoundException("Travel plan with ID " + planId + " not found");
        }

        if (plan.getStartDate() == null || plan.getEndDate() == null) {
            throw new RuntimeException("Travel plan dates are missing");
        }

        String destination = plan.getDestination();

        long daysBetween = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate());
        int numberOfDays = (int) daysBetween + 1;

        if (numberOfDays <= 0) {
            throw new RuntimeException("Invalid travel plan dates");
        }

        DailyPrices prices = getAverageDailyPrices(destination);

        double accommodationCost = prices.accommodationPerDay * numberOfDays;
        double foodCost = prices.foodPerDay * numberOfDays;
        double activitiesCost = prices.activitiesPerDay * numberOfDays;
        double transportCost = prices.transportPerDay * numberOfDays;

        double totalEstimatedCost =
                accommodationCost
                        + foodCost
                        + activitiesCost
                        + transportCost;

        return new BudgetEstimateResponse(
                plan.getId(),
                destination,
                numberOfDays,
                accommodationCost,
                foodCost,
                activitiesCost,
                transportCost,
                totalEstimatedCost,
                "EUR"
        );
    }

    private TravelPlanResponse getTravelPlanFromPlanningService(Long planId, String authHeader) {
        String url = "http://localhost:8082/api/travel-plans/" + planId;

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TravelPlanResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                TravelPlanResponse.class
        );

        return response.getBody();
    }

    private DailyPrices getAverageDailyPrices(String destination) {
        if (destination == null || destination.isBlank()) {
            return new DailyPrices(60, 25, 20, 15);
        }

        String normalizedDestination = destination.toLowerCase(Locale.ROOT);

        if (normalizedDestination.contains("paris")) {
            return new DailyPrices(120, 50, 40, 25);
        }

        if (normalizedDestination.contains("rome") || normalizedDestination.contains("rim")) {
            return new DailyPrices(90, 40, 35, 20);
        }

        if (normalizedDestination.contains("sarajevo")) {
            return new DailyPrices(50, 25, 20, 10);
        }

        if (normalizedDestination.contains("mostar")) {
            return new DailyPrices(45, 22, 18, 10);
        }

        if (normalizedDestination.contains("trebinje")) {
            return new DailyPrices(40, 20, 15, 8);
        }

        if (normalizedDestination.contains("dubrovnik")) {
            return new DailyPrices(100, 45, 35, 20);
        }

        return new DailyPrices(60, 25, 20, 15);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }

        String token = authHeader.substring(7);
        jwtValidator.getClaims(token);
    }

    private static class DailyPrices {

        private final double accommodationPerDay;
        private final double foodPerDay;
        private final double activitiesPerDay;
        private final double transportPerDay;

        private DailyPrices(
                double accommodationPerDay,
                double foodPerDay,
                double activitiesPerDay,
                double transportPerDay
        ) {
            this.accommodationPerDay = accommodationPerDay;
            this.foodPerDay = foodPerDay;
            this.activitiesPerDay = activitiesPerDay;
            this.transportPerDay = transportPerDay;
        }
    }
}
