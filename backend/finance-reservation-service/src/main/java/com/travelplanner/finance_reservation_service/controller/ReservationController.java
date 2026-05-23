package com.travelplanner.finance_reservation_service.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import com.travelplanner.finance_reservation_service.service.ReservationService;
import com.travelplanner.shared.security.JwtValidator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Manage travel reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final JwtValidator jwtUtils;

    @GetMapping
    @Operation(summary = "Get all reservations")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(reservationService.getAllReservations(authHeader));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<ReservationResponseDTO> getReservationById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(reservationService.getReservationById(id, authHeader));
    }

    @GetMapping("/plan/{planId}/paged")
    public ResponseEntity<List<Reservation>> getReservationsPaged(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestHeader("Authorization") String authHeader) {

        validateToken(authHeader); // Provjera jer ovdje direktno koristimo repository

        Sort sorting = direction.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sorting);
        
        return ResponseEntity.ok(reservationRepository.findByPlanId(planId, pageable).getContent());
    }

    @GetMapping("/plan/{planId}/premium")
    @Operation(summary = "Get reservations with price higher than minPrice")
    public ResponseEntity<List<Reservation>> getPremiumReservations(
            @PathVariable Long planId,
            @RequestParam Double minPrice,
            @RequestHeader("Authorization") String authHeader) {
        
        validateToken(authHeader); // Provjera za direktni repository poziv
        
        return ResponseEntity.ok(reservationRepository.findPremiumReservations(planId, minPrice));
    }

    @PostMapping
    @Operation(summary = "Create a reservation")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto, authHeader));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reservation")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        reservationService.deleteReservation(id, authHeader);
        return ResponseEntity.noContent().build();
    }

    // Pomoćna metoda za metode koje direktno koriste repository
    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        jwtUtils.getClaims(authHeader.substring(7));
    }
}
