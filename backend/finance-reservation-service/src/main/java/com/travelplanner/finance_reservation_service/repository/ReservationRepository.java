package com.travelplanner.finance_reservation_service.repository;

import com.travelplanner.finance_reservation_service.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByPlanId(UUID planId);
    List<Reservation> findByStatus(String status);
}
