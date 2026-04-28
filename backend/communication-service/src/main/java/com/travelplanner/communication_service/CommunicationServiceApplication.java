package com.travelplanner.communication_service;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import com.travelplanner.communication_service.model.Notification;
import com.travelplanner.communication_service.model.Review;
import com.travelplanner.communication_service.repository.NotificationRepository;
import com.travelplanner.communication_service.repository.ReviewRepository;
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class CommunicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicationServiceApplication.class, args);
	}

	
}