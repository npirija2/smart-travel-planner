package com.travelplanner.planning_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final DayRepository dayRepository;
    private final LocationRepository locationRepository;

    public List<ActivityResponseDTO> getAll() {
        return activityRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ActivityResponseDTO getById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity with id " + id + " not found"));
        return mapToResponseDTO(activity);
    }

    public List<ActivityResponseDTO> getByDayId(Long dayId) {
        return activityRepository.findByDayId(dayId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ActivityResponseDTO create(ActivityRequestDTO dto) {
        validate(dto);

        Day day = dayRepository.findById(dto.getDayId())
                .orElseThrow(() -> new ResourceNotFoundException("Day with id " + dto.getDayId() + " not found"));

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location with id " + dto.getLocationId() + " not found"));

        Activity activity = Activity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .day(day)
                .createdBy(dto.getCreatedBy())
                .location(location)
                .timeslot(dto.getTimeslot())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .duration(dto.getDuration())
                .status(dto.getStatus())
                .build();

        return mapToResponseDTO(activityRepository.save(activity));
    }

    public ActivityResponseDTO update(Long id, ActivityRequestDTO dto) {
        validate(dto);

        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity with id " + id + " not found"));

        Day day = dayRepository.findById(dto.getDayId())
                .orElseThrow(() -> new ResourceNotFoundException("Day with id " + dto.getDayId() + " not found"));

        Location location = locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location with id " + dto.getLocationId() + " not found"));

        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setDay(day);
        activity.setCreatedBy(dto.getCreatedBy());
        activity.setLocation(location);
        activity.setTimeslot(dto.getTimeslot());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setDuration(dto.getDuration());
        activity.setStatus(dto.getStatus());

        return mapToResponseDTO(activityRepository.save(activity));
    }

    public void delete(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activity with id " + id + " not found");
        }
        activityRepository.deleteById(id);
    }

    @Transactional
public ActivityResponseDTO addActivityToDay(Long dayId, ActivityRequestDTO dto) {

    Day day = dayRepository.findById(dayId)
            .orElseThrow(() -> new ResourceNotFoundException("Day not found"));

    Location location = locationRepository.findById(dto.getLocationId())
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

    Activity activity = Activity.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .day(day)
            .createdBy(dto.getCreatedBy())
            .location(location)
            .timeslot(dto.getTimeslot())
            .startTime(dto.getStartTime())
            .endTime(dto.getEndTime())
            .duration(dto.getDuration())
            .status(dto.getStatus())
            .build();

    return mapToResponseDTO(activityRepository.save(activity));
    }

    private void validate(ActivityRequestDTO dto) {
        if (dto.getDuration() != null && dto.getDuration() <= 0) {
            throw new BadRequestException("Duration must be greater than 0");
        }

        if (dto.getStartTime() != null && dto.getEndTime() != null &&
                dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private ActivityResponseDTO mapToResponseDTO(Activity activity) {
        return ActivityResponseDTO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .dayId(activity.getDay().getId())
                .dayDate(activity.getDay().getDate())
                .createdBy(activity.getCreatedBy())
                .locationId(activity.getLocation().getId())
                .locationName(activity.getLocation().getName())
                .timeslot(activity.getTimeslot())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .duration(activity.getDuration())
                .status(activity.getStatus())
                .build();
    }

     public boolean existsById(Long id) {
                return activityRepository.existsById(id);
        }
}