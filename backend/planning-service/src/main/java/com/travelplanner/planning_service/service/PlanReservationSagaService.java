package com.travelplanner.planning_service.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travelplanner.planning_service.config.RabbitMQConfig;
import com.travelplanner.planning_service.dto.PlanReservationRequestDTO;
import com.travelplanner.planning_service.messaging.event.PlanReservationRequestedEvent;
import com.travelplanner.planning_service.model.PlanReservation;
import com.travelplanner.planning_service.model.PlanReservationStatus;
import com.travelplanner.planning_service.repository.PlanReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanReservationSagaService {

    private final PlanReservationRepository planReservationRepository;
    private final TravelPlanService travelPlanService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Long requestReservation(Long planId, PlanReservationRequestDTO dto, String authHeader) {
        travelPlanService.getById(planId, authHeader);

        PlanReservation planReservation = PlanReservation.builder()
                .planId(planId)
                .userId(dto.getUserId())
                .reservationType(dto.getReservationType())
                .itemName(dto.getItemName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .status(PlanReservationStatus.PENDING)
                .build();

        PlanReservation saved = planReservationRepository.save(planReservation);

        PlanReservationRequestedEvent event = PlanReservationRequestedEvent.builder()
                .planId(planId)
                .planReservationId(saved.getId())
                .userId(dto.getUserId())
                .reservationType(dto.getReservationType())
                .itemName(dto.getItemName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .amount(dto.getAmount())
                .currency(dto.getCurrency())
                .simulateFinanceFailure(dto.isSimulateFinanceFailure())
                .simulatePlanningFinalizationFailure(dto.isSimulatePlanningFinalizationFailure())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.PLAN_RESERVATION_REQUESTED_KEY,
                event
        );

        return saved.getId();
    }
    public List<PlanReservation> getReservationsForPlan(Long planId) {
    return planReservationRepository.findByPlanId(planId);
}
}