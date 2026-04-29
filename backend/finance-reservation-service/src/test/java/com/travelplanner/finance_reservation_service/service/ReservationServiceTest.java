package com.travelplanner.finance_reservation_service.service;

import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.mapper.ReservationMapper;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation reservation;
    private ReservationRequestDTO requestDTO;
    private ReservationResponseDTO responseDTO;
    private UUID reservationId;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        
        reservation = new Reservation();
        reservation.setId(reservationId);
        
        requestDTO = new ReservationRequestDTO();
        requestDTO.setType("Hotel");
        
        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(reservationId);
        responseDTO.setType("Hotel");
    }

    @Test
    void getAllReservations_ShouldReturnList() {
        // Arrange
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));
        when(reservationMapper.toResponseDTO(any(Reservation.class))).thenReturn(responseDTO);

        // Act
        List<ReservationResponseDTO> result = reservationService.getAllReservations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getReservationById_WhenExists_ShouldReturnDTO() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationMapper.toResponseDTO(reservation)).thenReturn(responseDTO);

        // Act
        ReservationResponseDTO result = reservationService.getReservationById(reservationId);

        // Assert
        assertNotNull(result);
        assertEquals(reservationId, result.getId());
    }

    @Test
    void getReservationById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.getReservationById(reservationId);
        });
    }

    @Test
    void createReservation_ShouldSaveAndReturnDTO() {
        // Arrange
        when(reservationMapper.toEntity(any(ReservationRequestDTO.class))).thenReturn(reservation);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toResponseDTO(any(Reservation.class))).thenReturn(responseDTO);

        // Act
        ReservationResponseDTO result = reservationService.createReservation(requestDTO);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void updateReservation_WhenExists_ShouldUpdateAndReturnDTO() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationMapper.toResponseDTO(any(Reservation.class))).thenReturn(responseDTO);
        // updateEntityFromDTO je void metoda, Mockito je po defaultu ignoriše (doNothing)

        // Act
        ReservationResponseDTO result = reservationService.updateReservation(reservationId, requestDTO);

        // Assert
        assertNotNull(result);
        verify(reservationMapper).updateEntityFromDTO(eq(requestDTO), eq(reservation));
        verify(reservationRepository).save(reservation);
    }

    @Test
    void deleteReservation_WhenExists_ShouldDelete() {
        // Arrange
        when(reservationRepository.existsById(reservationId)).thenReturn(true);

        // Act
        reservationService.deleteReservation(reservationId);

        // Assert
        verify(reservationRepository, times(1)).deleteById(reservationId);
    }

    @Test
    void deleteReservation_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(reservationRepository.existsById(reservationId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.deleteReservation(reservationId);
        });
        verify(reservationRepository, never()).deleteById(any());
    }
}