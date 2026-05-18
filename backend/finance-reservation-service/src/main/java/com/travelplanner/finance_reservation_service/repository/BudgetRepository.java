package com.travelplanner.finance_reservation_service.repository;

import com.travelplanner.finance_reservation_service.dto.BudgetResponseDTO;
import com.travelplanner.finance_reservation_service.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByPlanId(Long planId);
}
