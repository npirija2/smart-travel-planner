package com.travelplanner.planning_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.travelplanner.planning_service.dto.DayRequestDTO;
import com.travelplanner.planning_service.dto.DayResponseDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final TravelPlanRepository travelPlanRepository;

    public DayResponseDTO create(DayRequestDTO dto) {

        TravelPlan plan = travelPlanRepository.findById(dto.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("TravelPlan not found"));

        Day day = Day.builder()
                .date(dto.getDate())
                .travelPlan(plan)
                .build();

        return map(dayRepository.save(day));
    }

    public List<DayResponseDTO> getAll() {
        return dayRepository.findAll().stream().map(this::map).toList();
    }

    private DayResponseDTO map(Day d) {
        return DayResponseDTO.builder()
                .id(d.getId())
                .date(d.getDate())
                .travelPlanId(d.getTravelPlan().getId())
                .build();
    }
}