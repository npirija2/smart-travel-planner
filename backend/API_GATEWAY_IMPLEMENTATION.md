# API Gateway Implementation

This document describes how the Spring Cloud API Gateway is configured for the Smart Travel Planner backend and how it fits into the current microservice architecture.

## Goal

The gateway is the single entry point for the backend. Instead of the frontend or external clients calling each microservice directly, requests go to the gateway first and are then routed to the correct downstream service through Eureka and Spring Cloud LoadBalancer.

## Existing Platform

The repository already contains:

- `eureka-server`
- `user-service`
- `planning-service`
- `communication-service`
- `finance-reservation-service`

Each service already has:

- a unique `spring.application.name`
- Eureka client configuration
- HTTP endpoints under `/api/...`

That means the gateway can route by service name using `lb://...` URIs.

## Added Module

A new module was added:

- `backend/api-gateway`

Main files:

- [ApiGatewayApplication.java](/Users/nejrapirija/smart-travel-planner/backend/api-gateway/src/main/java/com/travelplanner/api_gateway/ApiGatewayApplication.java)
- [application.yml](/Users/nejrapirija/smart-travel-planner/backend/api-gateway/src/main/resources/application.yml)

## Dependency Setup

The gateway module uses:

- `spring-cloud-starter-gateway`
- `spring-cloud-starter-loadbalancer`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-boot-starter-actuator`

This gives the gateway:

- reactive routing
- discovery-based service lookup
- load-balanced downstream calls
- actuator endpoints for route inspection

## Routing Strategy

The gateway uses explicit routes instead of automatically generated discovery routes.

Why explicit routes:

- the backend already exposes clear `/api/...` paths
- the public URL structure stays simple
- route behavior is easier to understand and document
- no path rewrite is required in the first version

### Route Map

#### User Service

- `/api/users/**` -> `lb://user-service`
- `/api/preferences/**` -> `lb://user-service`
- `/api/plan-memberships/**` -> `lb://user-service`

#### Planning Service

- `/api/travel-plans/**` -> `lb://planning-service`
- `/api/activities/**` -> `lb://planning-service`
- `/api/days/**` -> `lb://planning-service`
- `/api/categories/**` -> `lb://planning-service`
- `/api/destinations/**` -> `lb://planning-service`
- `/api/activity-categories/**` -> `lb://planning-service`
- `/api/locations/**` -> `lb://planning-service`

#### Communication Service

- `/api/notifications/**` -> `lb://communication-service`
- `/api/reviews/**` -> `lb://communication-service`
- `/api/votes/**` -> `lb://communication-service`
- `/api/shared-links/**` -> `lb://communication-service`

#### Finance Reservation Service

- `/api/budgets/**` -> `lb://finance-reservation-service`
- `/api/expenses/**` -> `lb://finance-reservation-service`
- `/api/reservations/**` -> `lb://finance-reservation-service`

## How Routing Works

When a request arrives at the gateway:

1. Spring Cloud Gateway checks the configured path predicates.
2. If a route matches, the gateway resolves the target service name through Eureka.
3. Spring Cloud LoadBalancer chooses a service instance.
4. The request is forwarded to that service instance.
5. The downstream service handles validation, business logic, and error handling.

This follows the normal Spring Cloud Gateway model where `lb://service-name` is used for load-balanced routing.

## CORS

Gateway-level CORS is configured globally so browser clients can call the backend through a single entry point.

Configured behavior:

- allowed origins: `*`
- allowed methods: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- allowed headers: `*`
- preflight requests are supported

This is more maintainable than duplicating CORS settings in every microservice.

## Actuator and Observability

The gateway exposes:

- `health`
- `info`
- `gateway`

This makes it possible to inspect route state and basic health from a single place.

## What Stayed the Same

The gateway does not replace the existing service responsibilities.

The downstream services still handle:

- validation
- business logic
- persistence
- error formatting

The gateway is only responsible for:

- routing
- discovery-based service lookup
- global CORS
- future cross-cutting concerns such as security or throttling

## Service Start Order

For local development, start the services in this order:

1. `eureka-server`
2. `user-service`
3. `planning-service`
4. `communication-service`
5. `finance-reservation-service`
6. `api-gateway`

Once all are running, the frontend can call the gateway instead of the individual service ports.

## Example Local Entry Point

Gateway URL:

- `http://localhost:8080`

Example routed requests:

- `http://localhost:8080/api/users`
- `http://localhost:8080/api/travel-plans`
- `http://localhost:8080/api/notifications`
- `http://localhost:8080/api/budgets`

## Validation and Error Handling

Validation and error handling remain inside the services and continue to return JSON responses.

The gateway does not duplicate those handlers. It simply forwards the requests and responses.

## Why This Design Fits the Project

This project already has:

- multiple microservices
- Eureka discovery
- consistent `/api/...` controller paths

That makes Spring Cloud Gateway a natural fit. The gateway gives the backend a single public entry point without forcing a redesign of the current services.

## Summary

The gateway implementation adds:

- a dedicated `api-gateway` module
- explicit Eureka-based routes to every microservice
- global CORS handling
- actuator visibility
- a clean public API entry point for the whole backend

This is the recommended place to centralize cross-cutting traffic concerns while keeping each microservice focused on its own domain logic.
