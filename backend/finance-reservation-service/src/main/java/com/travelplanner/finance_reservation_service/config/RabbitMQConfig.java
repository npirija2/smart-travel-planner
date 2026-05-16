package com.travelplanner.finance_reservation_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "travel.saga.exchange";

    public static final String PLAN_RESERVATION_REQUESTED_QUEUE = "plan.reservation.requested.queue";
    public static final String RESERVATION_CREATED_QUEUE = "reservation.created.queue";
    public static final String RESERVATION_FAILED_QUEUE = "reservation.failed.queue";
    public static final String PLAN_RESERVATION_CANCELLED_QUEUE = "plan.reservation.cancelled.queue";

    public static final String PLAN_RESERVATION_REQUESTED_KEY = "plan.reservation.requested";
    public static final String RESERVATION_CREATED_KEY = "reservation.created";
    public static final String RESERVATION_FAILED_KEY = "reservation.failed";
    public static final String PLAN_RESERVATION_CANCELLED_KEY = "plan.reservation.cancelled";

    @Bean
    public TopicExchange travelSagaExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue planReservationRequestedQueue() {
        return new Queue(PLAN_RESERVATION_REQUESTED_QUEUE, true);
    }

    @Bean
    public Queue reservationCreatedQueue() {
        return new Queue(RESERVATION_CREATED_QUEUE, true);
    }

    @Bean
    public Queue reservationFailedQueue() {
        return new Queue(RESERVATION_FAILED_QUEUE, true);
    }

    @Bean
    public Queue planReservationCancelledQueue() {
        return new Queue(PLAN_RESERVATION_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding planReservationRequestedBinding() {
        return BindingBuilder
                .bind(planReservationRequestedQueue())
                .to(travelSagaExchange())
                .with(PLAN_RESERVATION_REQUESTED_KEY);
    }

    @Bean
    public Binding reservationCreatedBinding() {
        return BindingBuilder
                .bind(reservationCreatedQueue())
                .to(travelSagaExchange())
                .with(RESERVATION_CREATED_KEY);
    }

    @Bean
    public Binding reservationFailedBinding() {
        return BindingBuilder
                .bind(reservationFailedQueue())
                .to(travelSagaExchange())
                .with(RESERVATION_FAILED_KEY);
    }

    @Bean
    public Binding planReservationCancelledBinding() {
        return BindingBuilder
                .bind(planReservationCancelledQueue())
                .to(travelSagaExchange())
                .with(PLAN_RESERVATION_CANCELLED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();

        typeMapper.setTrustedPackages(
                "com.travelplanner.planning_service.messaging.event",
                "com.travelplanner.finance_reservation_service.messaging.event"
        );
        // Use the listener method parameter type instead of the sender's FQCN header.
        typeMapper.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);

        return converter;
    }
}
