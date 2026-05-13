package com.travelplanner.finance_reservation_service.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.finance_reservation_service.config.RabbitMQConfig;
import com.travelplanner.finance_reservation_service.messaging.event.PlanReservationCancelledEvent;
import com.travelplanner.finance_reservation_service.messaging.event.PlanReservationRequestedEvent;
import com.travelplanner.finance_reservation_service.messaging.event.ReservationCreatedEvent;
import com.travelplanner.finance_reservation_service.messaging.event.ReservationFailedEvent;
import com.travelplanner.finance_reservation_service.model.SagaReservation;
import com.travelplanner.finance_reservation_service.model.SagaReservationStatus;
import com.travelplanner.finance_reservation_service.repository.SagaReservationRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FinanceReservationSagaListener {

    private final SagaReservationRepository sagaReservationRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.PLAN_RESERVATION_REQUESTED_QUEUE)
    @Transactional
    public void handlePlanReservationRequested(PlanReservationRequestedEvent event) {
        try {
            if (event.isSimulateFinanceFailure()) {
                throw new RuntimeException("Simulated finance reservation failure");
            }

            SagaReservation reservation = SagaReservation.builder()
                    .planId(event.getPlanId())
                    .planReservationId(event.getPlanReservationId())
                    .userId(event.getUserId())
                    .reservationType(event.getReservationType())
                    .itemName(event.getItemName())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .status(SagaReservationStatus.CONFIRMED)
                    .build();

            SagaReservation saved = sagaReservationRepository.save(reservation);

            String statusForPlanning = event.isSimulatePlanningFinalizationFailure()
                    ? "SIMULATE_PLANNING_FAILURE"
                    : "CONFIRMED";

            ReservationCreatedEvent createdEvent = ReservationCreatedEvent.builder()
                    .planId(event.getPlanId())
                    .planReservationId(event.getPlanReservationId())
                    .financeReservationId(saved.getId())
                    .status(statusForPlanning)
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RESERVATION_CREATED_KEY,
                    createdEvent
            );

        } catch (Exception ex) {
            ReservationFailedEvent failedEvent = ReservationFailedEvent.builder()
                    .planId(event.getPlanId())
                    .planReservationId(event.getPlanReservationId())
                    .reason(ex.getMessage())
                    .build();

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.RESERVATION_FAILED_KEY,
                    failedEvent
            );
        }
    }

    @RabbitListener(queues = RabbitMQConfig.PLAN_RESERVATION_CANCELLED_QUEUE)
    @Transactional
    public void handlePlanReservationCancelled(PlanReservationCancelledEvent event) {
        SagaReservation reservation = sagaReservationRepository.findById(event.getFinanceReservationId())
                .orElseThrow(() -> new RuntimeException("Finance reservation not found"));

        reservation.setStatus(SagaReservationStatus.CANCELLED);
        reservation.setFailureReason(event.getReason());

        sagaReservationRepository.save(reservation);
    }
}