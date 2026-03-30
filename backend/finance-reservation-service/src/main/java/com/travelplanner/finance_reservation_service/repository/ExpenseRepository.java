package com.travelplanner.finance_reservation_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.travelplanner.finance_reservation_service.model.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
}