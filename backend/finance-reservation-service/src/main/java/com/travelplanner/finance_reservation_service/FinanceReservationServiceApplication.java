package com.travelplanner.finance_reservation_service;

import java.time.LocalDateTime;
import java.util.UUID; 

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FinanceReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceReservationServiceApplication.class, args);
    }

    
}