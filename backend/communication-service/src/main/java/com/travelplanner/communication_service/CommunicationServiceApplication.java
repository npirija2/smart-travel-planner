package com.travelplanner.communication_service;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.NotificationRepository;
import com.travelplanner.communication_service.repository.ReviewRepository;
@EnableDiscoveryClient
@SpringBootApplication
public class CommunicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicationServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner run(NotificationRepository nRepo, ReviewRepository rRepo) {
		return args -> {

			Notification n = Notification.builder()
					.message("Plan created")
					.date(LocalDateTime.now())
					.userId(1)
					.planId(1)
					.type("INFO")
					.build();
			nRepo.save(n);

			Review r = Review.builder()
					.userId(1)
					.activityId(1)
					.rating(5)
					.comment("Great activity!")
					.build();
			rRepo.save(r);
		};
	}
}