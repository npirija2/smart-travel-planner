package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.TravelPlanRequestDTO;
import com.travelplanner.planning_service.dto.TravelPlanResponseDTO;
import com.travelplanner.planning_service.exception.BadRequestException;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.model.TravelPlan;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;
import com.travelplanner.planning_service.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TravelPlanServiceTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Mock
    private TravelPlanRepository travelPlanRepository;

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Claims claims;

    @InjectMocks
    private TravelPlanService travelPlanService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtUtils.getClaims("test-token")).thenReturn(claims);
        lenient().when(claims.getSubject()).thenReturn("1");
        lenient().when(claims.get("role")).thenReturn("ROLE_USER");
    }

    @Test
    void getById_returnsDto_whenExists() {
        Destination destination = Destination.builder()
                .id(2L)
                .name("Rim")
                .build();

        TravelPlan plan = TravelPlan.builder()
                .id(1L)
                .name("Plan")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 5))
                .ownerId(1L)
                .destination(destination)
                .description("Opis")
                .status("PLANNED")
                .build();

        given(travelPlanRepository.findById(1L)).willReturn(Optional.of(plan));

        TravelPlanResponseDTO result = travelPlanService.getById(1L, AUTH_HEADER);

        assertEquals("Plan", result.getName());
        assertEquals("Rim", result.getDestinationName());
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMissing() {
        given(travelPlanRepository.findById(9999L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> travelPlanService.getById(9999L, AUTH_HEADER));
    }

    @Test
    void create_throwsBadRequestException_whenEndDateBeforeStartDate() {
        TravelPlanRequestDTO request = TravelPlanRequestDTO.builder()
                .name("Pogresan plan")
                .startDate(LocalDate.of(2026, 8, 10))
                .endDate(LocalDate.of(2026, 8, 5))
                .destinationId(2L)
                .build();

        assertThrows(BadRequestException.class, () -> travelPlanService.create(request, AUTH_HEADER));
    }

    @Test
    void create_throwsResourceNotFoundException_whenDestinationMissing() {
        TravelPlanRequestDTO request = TravelPlanRequestDTO.builder()
                .name("Plan")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .destinationId(2L)
                .build();

        given(destinationRepository.findById(2L)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> travelPlanService.create(request, AUTH_HEADER));
    }

    @Test
    void create_savesTravelPlan_whenDataValid() {
        Destination destination = Destination.builder()
                .id(2L)
                .name("Rim")
                .build();

        TravelPlanRequestDTO request = TravelPlanRequestDTO.builder()
                .name("Plan")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .ownerId(1L)
                .destinationId(2L)
                .description("Opis")
                .status("PLANNED")
                .build();

        TravelPlan saved = TravelPlan.builder()
                .id(10L)
                .name("Plan")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 5))
                .ownerId(1L)
                .destination(destination)
                .description("Opis")
                .status("PLANNED")
                .build();

        given(destinationRepository.findById(2L)).willReturn(Optional.of(destination));
        given(travelPlanRepository.save(any(TravelPlan.class))).willReturn(saved);

        TravelPlanResponseDTO result = travelPlanService.create(request, AUTH_HEADER);

        assertEquals(10L, result.getId());
        assertEquals("Plan", result.getName());
        assertEquals("Rim", result.getDestinationName());
    }
}
