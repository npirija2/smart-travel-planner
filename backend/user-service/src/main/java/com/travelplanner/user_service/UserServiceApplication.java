package com.travelplanner.user_service;

import com.travelplanner.user_service.model.PlanUser;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.model.UserPreference;
import com.travelplanner.user_service.repository.PlanUserRepository;
import com.travelplanner.user_service.repository.UserPreferenceRepository;
import com.travelplanner.user_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(
            UserRepository userRepository,
            UserPreferenceRepository preferenceRepository,
            PlanUserRepository planUserRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                System.out.println("Starting full database seeding...");

                // 1. Create and Save User
                User admin = User.builder()
                        .username("nejra_admin")
                        .email("admin@travel-planner.com")
                        .passwordHash("hashed_pass_123")
                        .build();
                userRepository.save(admin);

                // 2. Create and Save Preferences linked to Admin
                UserPreference pref = UserPreference.builder()
                        .preferenceType("Currency")
                        .preferenceValue("EUR")
                        .user(admin)
                        .build();
                preferenceRepository.save(pref);

                // 3. Create and Save Plan Relation linked to Admin
                PlanUser planUser = PlanUser.builder()
                        .planId(101)
                        .role("CREATOR")
                        .user(admin)
                        .build();
                planUserRepository.save(planUser);

                System.out.println("Full seed completed: User, Preference, and Plan relation added!");
            }
        };
    }
}