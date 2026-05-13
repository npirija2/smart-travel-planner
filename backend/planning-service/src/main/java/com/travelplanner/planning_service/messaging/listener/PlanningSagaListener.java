package com.travelplanner.planning_service.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.planning_service.config.RabbitMQConfig;
import com.travelplanner.planning_service.messaging.event.PlanReservationCancelledEvent;
import com.travelplanner.planning_service.messaging.event.ReservationCreatedEvent;
import com.travelplanner.planning_service.messaging.event.ReservationFailedEvent;
import com.travelplanner.planning_service.model.PlanReservation;
import com.travelplanner.planning_service.model.PlanReservationStatus;
import com.travelplanner.planning_service.repository.PlanReservationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanningSagaListener {

    private final PlanReservationRepository planReservationRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_CREATED_QUEUE)
    @Transactional
    public void handleReservationCreated(ReservationCreatedEvent event) {
        PlanReservation planReservation = planReservationRepository.findById(event.getPlanReservationId())
                .orElseThrow(() -> new RuntimeException("Plan reservation not found"));

        try {
            if ("SIMULATE_PLANNING_FAILURE".equals(event.getStatus())) {
                throw new RuntimeException("Simulated planning finalization failure");
            }

            planReservation.setFinanceReservationId(event.getFinanceReservationId());
            planReservation.setStatus(PlanReservationStatus.CONFIRMED);
            planReservationRepository.save(planReservation);

        } catch (Exception ex) {
            planReservation.setStatus(PlanReservationStatus.FAILED);
            planReservation.setFailureReason(ex.getMessage());
            planReservationRepository.save(planReservation);

            PlanReservationCancelledEvent cancelledEvent = PlanReservationCancelledEvent.builder()
                    .planId(event.getPlanId())
                    .planReservationId(event.getPlanReservationId())
                    .financeReservationId(event.getFinanceReservationId())
                    .reason("Planning service failed to finalize reservation: " + ex.getMessage())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.PLAN_RESERVATION_CANCELLED_KEY,
                    cancelledEvent
            );
        }
    }

    @RabbitListener(queues = RabbitMQConfig.RESERVATION_FAILED_QUEUE)
    @Transactional
    public void handleReservationFailed(ReservationFailedEvent event) {
        PlanReservation planReservation = planReservationRepository.findById(event.getPlanReservationId())
                .orElseThrow(() -> new RuntimeException("Plan reservation not found"));

        planReservation.setStatus(PlanReservationStatus.CANCELLED);
        planReservation.setFailureReason(event.getReason());

        planReservationRepository.save(planReservation);
    }
}