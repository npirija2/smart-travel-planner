package com.travelplanner.planning_service;

import com.travelplanner.planning_service.model.*;
import com.travelplanner.planning_service.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class PlanningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanningServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(
            TravelPlanRepository travelPlanRepository,
            DayRepository dayRepository,
            LocationRepository locationRepository,
            ActivityRepository activityRepository,
            VoteRepository voteRepository
    ) {
        return args -> {
            if (travelPlanRepository.count() == 0) {

                TravelPlan plan = TravelPlan.builder()
                        .title("Putovanje u Rim")
                        .destination("Rim")
                        .startDate(LocalDate.of(2026, 6, 10))
                        .endDate(LocalDate.of(2026, 6, 12))
                        .status("PLANNED")
                        .optimizationPriority("distance")
                        .ownerId(1L)
                        .shareToken("rim-2026-token")
                        .build();

                travelPlanRepository.save(plan);

                Day day1 = Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 10))
                        .weatherSummary("Sunny")
                        .weatherTempC(28)
                        .build();

                Day day2 = Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 11))
                        .weatherSummary("Cloudy")
                        .weatherTempC(24)
                        .build();

                dayRepository.save(day1);
                dayRepository.save(day2);

                Location location1 = Location.builder()
                        .name("Colosseum")
                        .city("Rim")
                        .type("attraction")
                        .lat(41.8902)
                        .lng(12.4922)
                        .category("culture")
                        .rating(4.8)
                        .reviewCount(120000)
                        .openingHours("08:30-19:00")
                        .avgVisitTime(120)
                        .avgPricePerPerson(20.0)
                        .build();

                Location location2 = Location.builder()
                        .name("Trevi Fountain")
                        .city("Rim")
                        .type("attraction")
                        .lat(41.9009)
                        .lng(12.4833)
                        .category("culture")
                        .rating(4.7)
                        .reviewCount(95000)
                        .openingHours("Always open")
                        .avgVisitTime(45)
                        .avgPricePerPerson(0.0)
                        .build();

                locationRepository.save(location1);
                locationRepository.save(location2);

                Activity activity1 = Activity.builder()
                        .day(day1)
                        .location(location1)
                        .createdBy(1L)
                        .startTime(LocalDateTime.of(2026, 6, 10, 9, 0))
                        .endTime(LocalDateTime.of(2026, 6, 10, 11, 0))
                        .timeSlot("MORNING")
                        .cost(20.0)
                        .isConfirmed(true)
                        .priority(1)
                        .build();

                Activity activity2 = Activity.builder()
                        .day(day1)
                        .location(location2)
                        .createdBy(1L)
                        .startTime(LocalDateTime.of(2026, 6, 10, 17, 0))
                        .endTime(LocalDateTime.of(2026, 6, 10, 18, 0))
                        .timeSlot("EVENING")
                        .cost(0.0)
                        .isConfirmed(false)
                        .priority(2)
                        .build();

                activityRepository.save(activity1);
                activityRepository.save(activity2);

                Vote vote1 = Vote.builder()
                        .userId(2L)
                        .activity(activity2)
                        .voteType(1)
                        .build();

                Vote vote2 = Vote.builder()
                        .userId(3L)
                        .activity(activity2)
                        .voteType(1)
                        .build();

                voteRepository.save(vote1);
                voteRepository.save(vote2);
            }
        };
    }
}