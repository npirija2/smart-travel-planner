package com.travelplanner.finance_reservation_service.mapper;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponseDTO toResponseDTO(Reservation reservation) {
        if (reservation == null) return null;
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setType(reservation.getType());
        dto.setDetails(reservation.getDetails());
        dto.setPlanId(reservation.getPlanId());
        dto.setStartDate(reservation.getStartDate());
        dto.setEndDate(reservation.getEndDate());
        dto.setPrice(reservation.getPrice());
        dto.setStatus(reservation.getStatus());
        return dto;
    }

    public Reservation toEntity(ReservationRequestDTO dto) {
        if (dto == null) return null;
        return Reservation.builder()
                .type(dto.getType())
                .details(dto.getDetails())
                .planId(dto.getPlanId())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .price(dto.getPrice())
                .status(dto.getStatus())
                .build();
    }

    public void updateEntityFromDTO(ReservationRequestDTO dto, Reservation reservation) {
        if (dto == null || reservation == null) return;
        reservation.setType(dto.getType());
        reservation.setDetails(dto.getDetails());
        reservation.setPlanId(dto.getPlanId());
        reservation.setStartDate(dto.getStartDate());
        reservation.setEndDate(dto.getEndDate());
        reservation.setPrice(dto.getPrice());
        reservation.setStatus(dto.getStatus());
    }
}