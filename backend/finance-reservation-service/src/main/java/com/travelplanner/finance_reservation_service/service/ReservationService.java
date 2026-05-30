package com.travelplanner.finance_reservation_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.ReservationMapper;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import com.travelplanner.shared.security.JwtValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final JwtValidator jwtUtils;

    public List<ReservationResponseDTO> getAllReservations(String authHeader) {
        validateAuthorizationHeader(authHeader);
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    public List<ReservationResponseDTO> getReservationsByPlanId(Long planId, String authHeader) {
        validateAuthorizationHeader(authHeader);
        return reservationRepository.findByPlanId(planId).stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    public List<Reservation> getReservationsPaged(
            Long planId,
            int page,
            int size,
            String sortBy,
            String direction,
            String authHeader) {

        validateAuthorizationHeader(authHeader);

        Sort sorting = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sorting);

        return reservationRepository
                .findByPlanId(planId, pageable)
                .getContent();
    }

    public List<Reservation> getPremiumReservations(
            Long planId,
            Double minPrice,
            String authHeader) {

        validateAuthorizationHeader(authHeader);

        return reservationRepository
                .findPremiumReservations(planId, minPrice);
    }

    public ReservationResponseDTO getReservationById(UUID id, String authHeader) {
        validateAuthorizationHeader(authHeader);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + id + " not found"));
        return reservationMapper.toResponseDTO(reservation);
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto, String authHeader) {
        validateAuthorizationHeader(authHeader);
        Reservation reservation = reservationMapper.toEntity(dto);
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Transactional
    public ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO dto, String authHeader) {
        validateAuthorizationHeader(authHeader);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + id + " not found"));
        
        reservationMapper.updateEntityFromDTO(dto, reservation);
        
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteReservation(UUID id, String authHeader) {
        validateAuthorizationHeader(authHeader);
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation with ID " + id + " not found");
        }
        reservationRepository.deleteById(id);
    }

    private void validateAuthorizationHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        jwtUtils.validateToken(token);
    }
}
