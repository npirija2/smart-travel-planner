package com.travelplanner.finance_reservation_service.repository;

import com.travelplanner.finance_reservation_service.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findAllByPlanId(Long planId);
}
