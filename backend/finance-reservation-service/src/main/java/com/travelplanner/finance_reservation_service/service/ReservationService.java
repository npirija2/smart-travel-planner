package com.travelplanner.finance_reservation_service.service;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.ReservationMapper;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public List<ReservationResponseDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    public List<ReservationResponseDTO> getReservationsByPlanId(UUID planId) {
        return reservationRepository.findByPlanId(planId).stream()
                .map(reservationMapper::toResponseDTO)
                .toList();
    }

    public ReservationResponseDTO getReservationById(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + id + " not found"));
        return reservationMapper.toResponseDTO(reservation);
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO dto) {
        Reservation reservation = reservationMapper.toEntity(dto);
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Transactional
    public ReservationResponseDTO updateReservation(UUID id, ReservationRequestDTO dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation with ID " + id + " not found"));
        
        reservationMapper.updateEntityFromDTO(dto, reservation);
        
        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponseDTO(saved);
    }

    @Transactional
    public void deleteReservation(UUID id) {
        if (!reservationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reservation with ID " + id + " not found");
        }
        reservationRepository.deleteById(id);
    }
}