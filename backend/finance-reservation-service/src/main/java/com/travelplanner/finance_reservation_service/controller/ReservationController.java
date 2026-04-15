package com.travelplanner.finance_reservation_service.controller;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID; // Dodan import

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Manage travel reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @Operation(summary = "Get all reservations")
    public ResponseEntity<List<ReservationResponseDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    // Promijenjeno u UUID
    public ResponseEntity<ReservationResponseDTO> getReservationById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get reservations by plan ID")
    // Promijenjeno u UUID
    public ResponseEntity<List<ReservationResponseDTO>> getReservationsByPlanId(@PathVariable UUID planId) {
        return ResponseEntity.ok(reservationService.getReservationsByPlanId(planId));
    }

    @PostMapping
    @Operation(summary = "Create a reservation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a reservation")
    // Promijenjeno u UUID
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationRequestDTO dto) {
        return ResponseEntity.ok(reservationService.updateReservation(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a reservation")
    // Promijenjeno u UUID
    public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}