package com.travelplanner.finance_reservation_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.travelplanner.finance_reservation_service.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    List<Reservation> findByPlanId(Long planId);
    
    List<Reservation> findByStatus(String status);

    // 1. Custom upit (Netrivijalno - JPQL)
    @Query("SELECT r FROM Reservation r WHERE r.planId = :planId AND r.price >= :minPrice")
    List<Reservation> findPremiumReservations(@Param("planId") Long planId, @Param("minPrice") Double minPrice);

    // 2. Paginacija i sortiranje (Netrivijalno)
    // JpaRepository već podržava Pageable, samo treba definisati metodu ako želimo filtriranje
    Page<Reservation> findByPlanId(Long planId, Pageable pageable);
}
