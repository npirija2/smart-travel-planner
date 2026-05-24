package com.travelplanner.planning_service.service;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.planning_service.dto.TravelPlanBasicResponse;
import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.exception.BadRequestException;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Day;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.DayRepository;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;
import com.travelplanner.shared.security.JwtValidator;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final DestinationRepository destinationRepository;
    private final DayRepository dayRepository;
    private final JwtValidator jwtUtils;

    private Long getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        Claims claims = jwtUtils.getClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    private String getUserRoleFromToken(String authHeader) {
        String token = authHeader.substring(7);
        return (String) jwtUtils.getClaims(token).get("role");
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponseDTO> getAll(String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        String role = getUserRoleFromToken(authHeader);

        if ("ADMIN".equals(role)) {
            return travelPlanRepository.findAll().stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }

        return travelPlanRepository.findByOwnerId(userId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TravelPlanResponseDTO getById(Long id, String authHeader) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));

        Long userId = getUserIdFromToken(authHeader);
        String role = getUserRoleFromToken(authHeader);

        if (!travelPlan.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new BadRequestException("You are not authorized to view this plan");
        }

        return mapToResponseDTO(travelPlan);
    }

    @Transactional
    public TravelPlanResponseDTO create(TravelPlanRequestDTO dto, String authHeader) {
        validateDates(dto);
        Long userId = getUserIdFromToken(authHeader);

        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));

        TravelPlan travelPlan = TravelPlan.builder()
                .name(dto.getName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .ownerId(userId) 
                .destination(destination)
                .description(dto.getDescription())
                .status(dto.getStatus())
                .build();

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        synchronizeDays(savedPlan);
        return mapToResponseDTO(savedPlan);
    }

    @Transactional
    public TravelPlanResponseDTO update(Long id, TravelPlanRequestDTO dto, String authHeader) {
        validateDates(dto);
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));

        Long userId = getUserIdFromToken(authHeader);
        String role = getUserRoleFromToken(authHeader);

        if (!travelPlan.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new BadRequestException("You are not authorized to update this plan");
        }

        Destination destination = destinationRepository.findById(dto.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));

        travelPlan.setName(dto.getName());
        travelPlan.setStartDate(dto.getStartDate());
        travelPlan.setEndDate(dto.getEndDate());
        travelPlan.setDestination(destination);
        travelPlan.setDescription(dto.getDescription());
        travelPlan.setStatus(dto.getStatus());

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        synchronizeDays(savedPlan);
        return mapToResponseDTO(savedPlan);
    }

    @Transactional
    public void delete(Long id, String authHeader) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));

        Long userId = getUserIdFromToken(authHeader);
        String role = getUserRoleFromToken(authHeader);

        if (!travelPlan.getOwnerId().equals(userId) && !"ADMIN".equals(role)) {
            throw new BadRequestException("You are not authorized to delete this plan");
        }

        travelPlanRepository.delete(travelPlan);
    }

    private void validateDates(TravelPlanRequestDTO dto) {
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
    }

    private TravelPlanResponseDTO mapToResponseDTO(TravelPlan travelPlan) {
        return TravelPlanResponseDTO.builder()
                .id(travelPlan.getId())
                .name(travelPlan.getName())
                .startDate(travelPlan.getStartDate())
                .endDate(travelPlan.getEndDate())
                .ownerId(travelPlan.getOwnerId())
                .destinationId(travelPlan.getDestination() != null ? travelPlan.getDestination().getId() : null)
                .destinationName(travelPlan.getDestination() != null ? travelPlan.getDestination().getName() : null)
                .description(travelPlan.getDescription())
                .status(travelPlan.getStatus())
                .build();
    }

    private void synchronizeDays(TravelPlan travelPlan) {
        List<Day> existingDays = dayRepository.findByTravelPlanId(travelPlan.getId());
        java.util.Set<java.time.LocalDate> existingDates = existingDays.stream()
                .map(Day::getDate)
                .collect(java.util.stream.Collectors.toSet());

        Stream.iterate(travelPlan.getStartDate(), date -> !date.isAfter(travelPlan.getEndDate()), date -> date.plusDays(1))
                .filter(date -> !existingDates.contains(date))
                .map(date -> Day.builder()
                        .date(date)
                        .travelPlan(travelPlan)
                        .build())
                .forEach(dayRepository::save);
    }

    // 1. Dodaj metodu za paginaciju
    @Transactional(readOnly = true)
    public Page<TravelPlanResponseDTO> getAllPaged(Pageable pageable) {
        return travelPlanRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponseDTO> getByOwnerId(Long ownerId) {
        return travelPlanRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TravelPlanResponseDTO> getByStatus(String status) {
        return travelPlanRepository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TravelPlanResponseDTO getById(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Travel plan not found"));
        return mapToResponseDTO(travelPlan);
    }

    public TravelPlanBasicResponse getTravelPlanBasicById(Long id) {
        TravelPlan plan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel plan not found"));

        String destinationName = null;

        if (plan.getDestination() != null) {
            destinationName = plan.getDestination().getName();
        }

        System.out.println("PLAN ID = " + plan.getId());
        System.out.println("PLAN DESTINATION OBJECT = " + plan.getDestination());
        System.out.println("DESTINATION NAME = " + destinationName);

        return new TravelPlanBasicResponse(
                plan.getId(),
                null,
                destinationName,
                plan.getStartDate(),
                plan.getEndDate()
        );
    }
}
