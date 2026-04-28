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

    @Bean
    CommandLineRunner run(BudgetRepository bRepo,
                          ExpenseRepository eRepo,
                          ReservationRepository rRepo) {
        return args -> {
            UUID sharedPlanId = UUID.randomUUID();

            Budget b = Budget.builder()
                    .totalAmount(1000.0)
                    .planId(sharedPlanId) 
                    .currency("EUR")
                    .build();
            bRepo.save(b);

            Expense e = Expense.builder()
                    .amount(200.0)
                    .planId(sharedPlanId) 
                    .category("Food")
                    .date(LocalDateTime.now())
                    .build();
            eRepo.save(e);

            Reservation r = Reservation.builder()
                    .type("Hotel")
                    .details("Sarajevo")
                    .planId(sharedPlanId) 
                    .startDate(LocalDateTime.now())
                    .endDate(LocalDateTime.now().plusDays(2))
                    .price(300.0)
                    .status("CONFIRMED")
                    .build();
            rRepo.save(r);

            System.out.println("--- Testni podaci ubačeni sa Plan ID: " + sharedPlanId + " ---");
        };
    }
}