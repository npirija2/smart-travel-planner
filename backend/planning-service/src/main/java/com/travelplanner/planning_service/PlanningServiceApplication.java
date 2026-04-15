package com.travelplanner.planning_service;

import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.LocationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalTime;

@SpringBootApplication
public class PlanningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanningServiceApplication.class, args);
    }

    @Profile("!test")
    @Bean
    CommandLineRunner seedData(
            DestinationRepository destinationRepository,
            TravelPlanRepository travelPlanRepository,
            DayRepository dayRepository,
            LocationRepository locationRepository,
            ActivityRepository activityRepository
    ) {
        return args -> {
            if (destinationRepository.count() == 0) {

                Destination destination = Destination.builder()
                        .name("Rim")
                        .build();

                destinationRepository.save(destination);

                TravelPlan plan = TravelPlan.builder()
                        .name("Putovanje u Rim")
                        .startDate(LocalDate.of(2026, 6, 10))
                        .endDate(LocalDate.of(2026, 6, 12))
                        .ownerId(1L)
                        .destination(destination)
                        .description("Trodnevni plan posjete Rimu")
                        .status("PLANNED")
                        .build();

                travelPlanRepository.save(plan);

                Day day1 = Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 10))
                        .build();

                Day day2 = Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 11))
                        .build();

                dayRepository.save(day1);
                dayRepository.save(day2);

                Location location1 = Location.builder()
                        .name("Colosseum")
                        .destination(destination)
                        .address("Piazza del Colosseo, Rome")
                        .latitude(41.8902)
                        .longitude(12.4922)
                        .type("ATTRACTION")
                        .build();

                Location location2 = Location.builder()
                        .name("Trevi Fountain")
                        .destination(destination)
                        .address("Piazza di Trevi, Rome")
                        .latitude(41.9009)
                        .longitude(12.4833)
                        .type("ATTRACTION")
                        .build();

                locationRepository.save(location1);
                locationRepository.save(location2);

                Activity activity1 = Activity.builder()
                        .name("Posjeta Koloseumu")
                        .description("Jutarnji obilazak Koloseuma")
                        .day(day1)
                        .createdBy(1L)
                        .location(location1)
                        .timeslot("MORNING")
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(11, 0))
                        .duration(120)
                        .status("PLANNED")
                        .build();

                Activity activity2 = Activity.builder()
                        .name("Posjeta Fontani di Trevi")
                        .description("Večernja šetnja i obilazak")
                        .day(day1)
                        .createdBy(1L)
                        .location(location2)
                        .timeslot("EVENING")
                        .startTime(LocalTime.of(17, 0))
                        .endTime(LocalTime.of(18, 0))
                        .duration(60)
                        .status("PLANNED")
                        .build();

                activityRepository.save(activity1);
                activityRepository.save(activity2);
            }
        };
    }
}
