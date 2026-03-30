package com.travelplanner.finance_reservation_service;

import java.time.LocalDateTime;

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

			Budget b = Budget.builder()
					.totalAmount(1000.0)
					.planId(1)
					.currency("EUR")
					.build();
			bRepo.save(b);

			Expense e = Expense.builder()
					.amount(200.0)
					.planId(1)
					.category("Food")
					.date(LocalDateTime.now())
					.build();
			eRepo.save(e);

			Reservation r = Reservation.builder()
					.type("Hotel")
					.details("Sarajevo")
					.planId(1)
					.startDate(LocalDateTime.now())
					.endDate(LocalDateTime.now().plusDays(2))
					.price(300.0)
					.status("CONFIRMED")
					.build();
			rRepo.save(r);
		};
	}
}