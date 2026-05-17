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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, Destination> destinationsByName = new HashMap<>();
            destinationRepository.findAll().forEach(destination -> destinationsByName.put(destination.getName(), destination));

            Destination rome = destinationsByName.computeIfAbsent("Rim", name ->
                    destinationRepository.save(Destination.builder().name(name).build()));
            Destination paris = destinationsByName.computeIfAbsent("Pariz", name ->
                    destinationRepository.save(Destination.builder().name(name).build()));
            Destination barcelona = destinationsByName.computeIfAbsent("Barcelona", name ->
                    destinationRepository.save(Destination.builder().name(name).build()));

            Map<String, Location> locationsByKey = new HashMap<>();
            destinationsByName.forEach((destinationName, destination) ->
                    locationRepository.findByDestinationId(destination.getId()).forEach(location ->
                            locationsByKey.put(destinationName + "::" + location.getName(), location)));

            seedLocation(locationRepository, locationsByKey, rome, "Colosseum", "Piazza del Colosseo, Rome", 41.8902, 12.4922, "historic");
            seedLocation(locationRepository, locationsByKey, rome, "Trevi Fountain", "Piazza di Trevi, Rome", 41.9009, 12.4833, "culture");
            seedLocation(locationRepository, locationsByKey, rome, "Villa Borghese", "Piazzale Napoleone I, Rome", 41.9142, 12.4923, "park");
            seedLocation(locationRepository, locationsByKey, rome, "Trastevere Food Walk", "Trastevere, Rome", 41.8897, 12.4700, "restaurant");
            seedLocation(locationRepository, locationsByKey, rome, "Roman Forum Night Tour", "Via della Salara Vecchia, Rome", 41.8925, 12.4853, "event");

            seedLocation(locationRepository, locationsByKey, paris, "Louvre Museum", "Rue de Rivoli, Paris", 48.8606, 2.3376, "museum");
            seedLocation(locationRepository, locationsByKey, paris, "Eiffel Tower", "Champ de Mars, Paris", 48.8584, 2.2945, "historic");
            seedLocation(locationRepository, locationsByKey, paris, "Luxembourg Gardens", "Rue de Medicis, Paris", 48.8462, 2.3371, "park");
            seedLocation(locationRepository, locationsByKey, paris, "Le Marais Bistro", "Le Marais, Paris", 48.8576, 2.3622, "restaurant");
            seedLocation(locationRepository, locationsByKey, paris, "Seine Evening Cruise", "Port de la Bourdonnais, Paris", 48.8600, 2.2979, "entertainment");

            seedLocation(locationRepository, locationsByKey, barcelona, "Sagrada Familia", "Carrer de Mallorca, Barcelona", 41.4036, 2.1744, "culture");
            seedLocation(locationRepository, locationsByKey, barcelona, "Park Guell", "Carrer d'Olot, Barcelona", 41.4145, 2.1527, "nature");
            seedLocation(locationRepository, locationsByKey, barcelona, "Gothic Quarter Walk", "Barri Gotic, Barcelona", 41.3839, 2.1760, "historic");
            seedLocation(locationRepository, locationsByKey, barcelona, "La Boqueria", "La Rambla, Barcelona", 41.3826, 2.1714, "food");
            seedLocation(locationRepository, locationsByKey, barcelona, "Flamenco Night", "El Raval, Barcelona", 41.3799, 2.1682, "event");

            if (travelPlanRepository.count() == 0) {
                TravelPlan plan = TravelPlan.builder()
                        .name("Putovanje u Rim")
                        .startDate(LocalDate.of(2026, 6, 10))
                        .endDate(LocalDate.of(2026, 6, 12))
                        .ownerId(1L)
                        .destination(rome)
                        .description("Trodnevni plan posjete Rimu")
                        .status("PLANNED")
                        .build();

                travelPlanRepository.save(plan);

                Day day1 = dayRepository.save(Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 10))
                        .build());

                Day day2 = dayRepository.save(Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 11))
                        .build());

                Day day3 = dayRepository.save(Day.builder()
                        .travelPlan(plan)
                        .date(LocalDate.of(2026, 6, 12))
                        .build());

                Map<String, Location> romeLocations = mapLocationsForDestination(locationRepository.findByDestinationId(rome.getId()));

                activityRepository.saveAll(List.of(
                        Activity.builder()
                                .name("Posjeta Koloseumu")
                                .description("Jutarnji obilazak Koloseuma")
                                .day(day1)
                                .createdBy(1L)
                                .location(romeLocations.get("Colosseum"))
                                .timeslot("MORNING")
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(11, 0))
                                .duration(120)
                                .status("PLANNED")
                                .build(),
                        Activity.builder()
                                .name("Fontana di Trevi")
                                .description("Lagani obilazak centra i fotografisanje")
                                .day(day1)
                                .createdBy(1L)
                                .location(romeLocations.get("Trevi Fountain"))
                                .timeslot("AFTERNOON")
                                .startTime(LocalTime.of(14, 0))
                                .endTime(LocalTime.of(15, 0))
                                .duration(60)
                                .status("PLANNED")
                                .build(),
                        Activity.builder()
                                .name("Šetnja kroz Villa Borghese")
                                .description("Predah u parku i panoramski pogled")
                                .day(day2)
                                .createdBy(1L)
                                .location(romeLocations.get("Villa Borghese"))
                                .timeslot("MORNING")
                                .startTime(LocalTime.of(9, 30))
                                .endTime(LocalTime.of(11, 0))
                                .duration(90)
                                .status("PLANNED")
                                .build(),
                        Activity.builder()
                                .name("Večera u Trastevereu")
                                .description("Lokalna hrana i opuštena večernja atmosfera")
                                .day(day2)
                                .createdBy(1L)
                                .location(romeLocations.get("Trastevere Food Walk"))
                                .timeslot("EVENING")
                                .startTime(LocalTime.of(19, 0))
                                .endTime(LocalTime.of(20, 30))
                                .duration(90)
                                .status("PLANNED")
                                .build(),
                        Activity.builder()
                                .name("Noćni obilazak Rimskog foruma")
                                .description("Kulturni događaj za završetak putovanja")
                                .day(day3)
                                .createdBy(1L)
                                .location(romeLocations.get("Roman Forum Night Tour"))
                                .timeslot("EVENING")
                                .startTime(LocalTime.of(18, 30))
                                .endTime(LocalTime.of(20, 0))
                                .duration(90)
                                .status("PLANNED")
                                .build()
                ));
            }
        };
    }

    private static void seedLocation(
            LocationRepository locationRepository,
            Map<String, Location> locationsByKey,
            Destination destination,
            String name,
            String address,
            double latitude,
            double longitude,
            String type
    ) {
        String key = destination.getName() + "::" + name;
        if (locationsByKey.containsKey(key)) {
            return;
        }

        Location savedLocation = locationRepository.save(Location.builder()
                .name(name)
                .destination(destination)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .type(type)
                .build());

        locationsByKey.put(key, savedLocation);
    }

    private static Map<String, Location> mapLocationsForDestination(List<Location> locations) {
        Map<String, Location> result = new HashMap<>();
        locations.forEach(location -> result.put(location.getName(), location));
        return result;
    }
}
