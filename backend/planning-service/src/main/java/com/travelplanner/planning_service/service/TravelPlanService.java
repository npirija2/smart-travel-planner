package com.travelplanner.planning_service.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.exception.BadRequestException;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRepository destinationRepository;

    public List<TravelPlanResponseDTO> getAll() {
        return travelPlanRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public TravelPlanResponseDTO getById(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan with id " + id + " not found"));
        return mapToResponseDTO(travelPlan);
    }

    public TravelPlanResponseDTO create(TravelPlanRequestDTO dto) {
        validateDates(dto);

        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination with id " + dto.getDestinationId() + " not found"));

        TravelPlan travelPlan = TravelPlan.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .ownerId(dto.getOwnerId())
                .destination(destination)
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();

        return mapToResponseDTO(travelPlanRepository.save(travelPlan));
    }

    public TravelPlanResponseDTO update(Long id, TravelPlanRequestDTO dto) {
        validateDates(dto);

        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan with id " + id + " not found"));

        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination with id " + dto.getDestinationId() + " not found"));

        travelPlan.setName(dto.getName());
        travelPlan.setStartDate(dto.getStartDate());
        travelPlan.setEndDate(dto.getEndDate());
        travelPlan.setOwnerId(dto.getOwnerId());
        travelPlan.setDestination(destination);
        travelPlan.setDescription(dto.getDescription());
        travelPlan.setStatus(dto.getStatus());

        return mapToResponseDTO(travelPlanRepository.save(travelPlan));
    }

    public void delete(Long id) {
        if (!travelPlanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Travel plan with id " + id + " not found");
        }
        travelPlanRepository.deleteById(id);
    }

    private void validateDates(TravelPlanRequestDTO dto) {
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
    }

    public Page<TravelPlanResponseDTO> getAllPaged(Pageable pageable) {
    return travelPlanRepository.findAll(pageable)
            .map(this::mapToResponseDTO);
    }

    public List<TravelPlanResponseDTO> getByOwnerId(Long ownerId) {
    return travelPlanRepository.findByOwnerId(ownerId)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }

    public List<TravelPlanResponseDTO> getByStatus(String status) {
    return travelPlanRepository.findByStatus(status)
            .stream()
            .map(this::mapToResponseDTO)
            .toList();
    }

    private TravelPlanResponseDTO mapToResponseDTO(TravelPlan travelPlan) {
    return TravelPlanResponseDTO.builder()
            .id(travelPlan.getId())
            .name(travelPlan.getName())
            .startDate(travelPlan.getStartDate())
            .endDate(travelPlan.getEndDate())
            .ownerId(travelPlan.getOwnerId())
            .destinationId(
                    travelPlan.getDestination() != null ? travelPlan.getDestination().getId() : null
            )
            .destinationName(
                    travelPlan.getDestination() != null ? travelPlan.getDestination().getName() : null
            )
            .description(travelPlan.getDescription())
            .status(travelPlan.getStatus())
            .build();
}
}
