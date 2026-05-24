package com.travelplanner.finance_reservation_service;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.travelplanner.finance_reservation_service.model.Budget;
import com.travelplanner.finance_reservation_service.model.Expense;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.finance_reservation_service.repository.ExpenseRepository;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.travelplanner")
@Slf4j
public class FinanceReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceReservationServiceApplication.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner run(BudgetRepository bRepo,
                          ExpenseRepository eRepo,
                          ReservationRepository rRepo) {
        return args -> {
            Long sharedPlanId = 1L;

            if (bRepo.count() == 0) {
                Budget b = Budget.builder()
                        .totalAmount(1000.0)
                        .planId(sharedPlanId)
                        .currency("EUR")
                        .build();
                bRepo.save(b);
            }

            if (eRepo.count() == 0) {
                Expense e = Expense.builder()
                        .amount(200.0)
                        .planId(sharedPlanId)
                        .category("Food")
                        .date(LocalDateTime.now())
                        .build();
                eRepo.save(e);
            }

            if (rRepo.count() == 0) {
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
            }

            log.info("Seeded finance reservation test data with shared plan ID {}", sharedPlanId);
        };
    }
}
