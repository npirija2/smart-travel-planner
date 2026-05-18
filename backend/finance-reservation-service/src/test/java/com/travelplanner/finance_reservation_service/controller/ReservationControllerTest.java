package com.travelplanner.finance_reservation_service.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.finance_reservation_service.dto.ReservationRequestDTO;
import com.travelplanner.finance_reservation_service.dto.ReservationResponseDTO;
import com.travelplanner.finance_reservation_service.exception.ResourceNotFoundException;
import com.travelplanner.finance_reservation_service.model.Reservation;
import com.travelplanner.finance_reservation_service.repository.ReservationRepository;
import com.travelplanner.finance_reservation_service.service.ReservationService;
import com.travelplanner.finance_reservation_service.util.JwtUtils;

import io.jsonwebtoken.Claims;

@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
class ReservationControllerTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private ReservationResponseDTO responseDTO;
    private ReservationRequestDTO requestDTO;
    private UUID reservationId;
    private Long planId;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        planId = 303L;
        LocalDateTime now = LocalDateTime.now();

        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(reservationId);
        responseDTO.setPlanId(planId);
        responseDTO.setType("Hotel");
        responseDTO.setPrice(250.0);
        // Umjesto setDate, koristiš ova dva:
        responseDTO.setStartDate(now);
        responseDTO.setEndDate(now.plusDays(2)); 
        responseDTO.setStatus("CONFIRMED");

        requestDTO = new ReservationRequestDTO();
        requestDTO.setPlanId(planId);
        requestDTO.setType("Hotel");
        requestDTO.setPrice(250.0);
        // Umjesto setDate, koristiš ova dva:
        requestDTO.setStartDate(now);
        requestDTO.setEndDate(now.plusDays(2));
        requestDTO.setStatus("CONFIRMED");
    }

    @Test
    void shouldGetAllReservations() throws Exception {
        when(reservationService.getAllReservations(AUTH_HEADER)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/reservations").header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].price").value(250.0));
    }

    @Test
    void shouldGetReservationById() throws Exception {
        when(reservationService.getReservationById(reservationId, AUTH_HEADER)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/reservations/" + reservationId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId.toString()));
    }

    @Test
    void shouldReturn404WhenReservationNotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(reservationService.getReservationById(randomId, AUTH_HEADER))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/reservations/" + randomId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateReservation() throws Exception {
        when(reservationService.createReservation(any(ReservationRequestDTO.class), eq(AUTH_HEADER))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/reservations")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(250.0));
    }

    @Test
    void shouldGetReservationsPaged() throws Exception {
        // Za paged test moramo napraviti instancu modela (Reservation) jer kontroler to vraća
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setPlanId(planId);
        reservation.setPrice(250.0);

        when(jwtUtils.getClaims("test-token")).thenReturn(mock(Claims.class));
        when(reservationRepository.findByPlanId(eq(planId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(reservation)));

        mockMvc.perform(get("/api/reservations/plan/" + planId + "/paged")
                .header("Authorization", AUTH_HEADER)
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "price")
                .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetPremiumReservations() throws Exception {
        Reservation premiumRes = new Reservation();
        premiumRes.setPrice(500.0);

        when(jwtUtils.getClaims("test-token")).thenReturn(mock(Claims.class));
        when(reservationRepository.findPremiumReservations(eq(planId), any(Double.class)))
                .thenReturn(List.of(premiumRes));

        mockMvc.perform(get("/api/reservations/plan/" + planId + "/premium")
                .header("Authorization", AUTH_HEADER)
                .param("minPrice", "300.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(500.0));
    }

    @Test
    void shouldDeleteReservation() throws Exception {
        doNothing().when(reservationService).deleteReservation(reservationId, AUTH_HEADER);

        mockMvc.perform(delete("/api/reservations/" + reservationId).header("Authorization", AUTH_HEADER))
                .andExpect(status().isNoContent());
    }
}
