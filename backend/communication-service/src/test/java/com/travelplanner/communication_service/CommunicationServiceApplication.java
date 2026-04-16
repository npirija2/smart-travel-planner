package com.travelplanner.communication_service;

import com.travelplanner.communication_service.repository.NotificationRepository;
import com.travelplanner.communication_service.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class CommunicationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunicationServiceApplication.class, args);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner run(NotificationRepository notificationRepository,
                          ReviewRepository reviewRepository) {
        return args -> {
        };
    }
}