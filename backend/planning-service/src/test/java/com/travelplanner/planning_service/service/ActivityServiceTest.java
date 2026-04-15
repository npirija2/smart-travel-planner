package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.ActivityRequestDTO;
import com.travelplanner.planning_service.dto.ActivityResponseDTO;
import com.travelplanner.planning_service.exception.BadRequestException;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.Location;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private DayRepository dayRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void create_shouldSaveActivity() {

        Day day = Day.builder()
                .id(1L)
                .date(LocalDate.of(2026, 6, 17))
                .build();

        Location location = Location.builder()
                .id(1L)
                .name("Colosseum")
                .build();

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .description("Test opis")
                .dayId(1L)
                .createdBy(1L)
                .locationId(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .status("PLANNED")
                .build();

        Activity saved = Activity.builder()
                .id(1L)
                .name("Test")
                .description("Test opis")
                .day(day)
                .location(location)
                .createdBy(1L)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .status("PLANNED")
                .build();

        given(dayRepository.findById(1L)).willReturn(Optional.of(day));
        given(locationRepository.findById(1L)).willReturn(Optional.of(location));
        given(activityRepository.save(any(Activity.class))).willReturn(saved);

        ActivityResponseDTO result = activityService.create(request);

        assertEquals("Test", result.getName());
        assertEquals("Colosseum", result.getLocationName());
    }

    @Test
    void create_shouldThrow_whenDayMissing() {

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .dayId(99L)
                .locationId(1L)
                .build();

        given(dayRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> activityService.create(request));
    }

    @Test
    void create_shouldThrow_whenLocationMissing() {

        Day day = Day.builder().id(1L).build();

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .dayId(1L)
                .locationId(99L)
                .build();

        given(dayRepository.findById(1L)).willReturn(Optional.of(day));
        given(locationRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> activityService.create(request));
    }

    @Test
    void create_shouldThrow_whenDurationInvalid() {

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .dayId(1L)
                .locationId(1L)
                .duration(0)
                .build();

        assertThrows(BadRequestException.class,
                () -> activityService.create(request));
    }

    @Test
    void create_shouldThrow_whenTimeInvalid() {

        ActivityRequestDTO request = ActivityRequestDTO.builder()
                .name("Test")
                .dayId(1L)
                .locationId(1L)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(9, 0))
                .duration(60)
                .build();

        assertThrows(BadRequestException.class,
                () -> activityService.create(request));
    }
}