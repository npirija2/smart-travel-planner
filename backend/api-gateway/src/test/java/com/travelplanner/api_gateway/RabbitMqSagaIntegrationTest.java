package com.travelplanner.api_gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@EnabledIfSystemProperty(named = "live.rabbitmq.saga", matches = "true")
class RabbitMqSagaIntegrationTest {

    private static final String SECRET = "04ca678afcf2124351ea89602209537307981873b9875af1db0b222460ed6d10";
    private static final long USER_ID = Long.getLong("saga.test.user-id", 42L);
    private static final String USER_ROLE = System.getProperty("saga.test.user-role", "ROLE_USER");
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(
            Long.getLong("saga.poll.timeout.seconds", 20L)
    );
    private static final Duration POLL_INTERVAL = Duration.ofMillis(
            Long.getLong("saga.poll.interval.millis", 500L)
    );

    private final String planningBaseUrl = System.getProperty("planning.base-url", "http://localhost:8082");
    private final String financeBaseUrl = System.getProperty("finance.base-url", "http://localhost:8083");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final String authHeader = "Bearer " + generateJwtToken(USER_ID, USER_ROLE);

    @Test
    void shouldConfirmReservationAcrossPlanningAndFinance() throws Exception {
        long destinationId = createDestination("RabbitMQ happy path " + UUID.randomUUID());
        long planId = createTravelPlan(destinationId, "Happy path plan " + UUID.randomUUID());
        long planReservationId = requestReservation(planId, false, false);

        JsonNode planningReservation = awaitPlanningReservation(planId, planReservationId, "CONFIRMED");
        JsonNode financeReservation = awaitFinanceReservation(planId, planReservationId, "CONFIRMED");

        assertThat(planningReservation.path("status").asText()).isEqualTo("CONFIRMED");
        assertThat(planningReservation.path("financeReservationId").asLong()).isPositive();
        assertThat(financeReservation.path("status").asText()).isEqualTo("CONFIRMED");
        assertThat(financeReservation.path("planReservationId").asLong()).isEqualTo(planReservationId);
    }

    @Test
    void shouldCompensateFinanceReservationWhenPlanningFinalizationFails() throws Exception {
        long destinationId = createDestination("RabbitMQ compensation " + UUID.randomUUID());
        long planId = createTravelPlan(destinationId, "Compensation plan " + UUID.randomUUID());
        long planReservationId = requestReservation(planId, false, true);

        JsonNode planningReservation = awaitPlanningReservation(planId, planReservationId, "FAILED");
        JsonNode financeReservation = awaitFinanceReservation(planId, planReservationId, "CANCELLED");

        assertThat(planningReservation.path("status").asText()).isEqualTo("FAILED");
        assertThat(planningReservation.path("failureReason").asText())
                .contains("Simulated planning finalization failure");
        assertThat(financeReservation.path("status").asText()).isEqualTo("CANCELLED");
        assertThat(financeReservation.path("failureReason").asText())
                .contains("Planning service failed to finalize reservation");
    }

    private long createDestination(String destinationName) throws Exception {
        JsonNode response = sendJsonRequest(
                "POST",
                planningBaseUrl + "/api/destinations",
                objectMapper.writeValueAsString(Map.of("name", destinationName)),
                null,
                201
        );

        return response.path("id").asLong();
    }

    private long createTravelPlan(long destinationId, String planName) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "name", planName,
                "startDate", LocalDate.now().plusDays(10).toString(),
                "endDate", LocalDate.now().plusDays(15).toString(),
                "destinationId", destinationId,
                "description", "Live RabbitMQ saga integration test",
                "status", "PLANNED"
        );

        JsonNode response = sendJsonRequest(
                "POST",
                planningBaseUrl + "/api/travel-plans",
                objectMapper.writeValueAsString(requestBody),
                authHeader,
                201
        );

        return response.path("id").asLong();
    }

    private long requestReservation(long planId, boolean simulateFinanceFailure,
                                    boolean simulatePlanningFinalizationFailure) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "userId", USER_ID,
                "reservationType", "HOTEL",
                "itemName", "Integration Test Hotel",
                "startDate", LocalDate.now().plusDays(10).toString(),
                "endDate", LocalDate.now().plusDays(15).toString(),
                "amount", new BigDecimal("199.99"),
                "currency", "EUR",
                "simulateFinanceFailure", simulateFinanceFailure,
                "simulatePlanningFinalizationFailure", simulatePlanningFinalizationFailure
        );

        JsonNode response = sendJsonRequest(
                "POST",
                planningBaseUrl + "/api/travel-plans/" + planId + "/reservations",
                objectMapper.writeValueAsString(requestBody),
                authHeader,
                202
        );

        return response.path("planReservationId").asLong();
    }

    private JsonNode awaitPlanningReservation(long planId, long planReservationId, String expectedStatus) throws Exception {
        return awaitReservation(
                "planning reservation " + planReservationId + " to become " + expectedStatus,
                () -> {
                    JsonNode response = sendJsonRequest(
                            "GET",
                            planningBaseUrl + "/api/travel-plans/" + planId + "/reservations",
                            null,
                            null,
                            200
                    );
                    return findReservationById(response, "id", planReservationId)
                            .filter(node -> expectedStatus.equals(node.path("status").asText()));
                }
        );
    }

    private JsonNode awaitFinanceReservation(long planId, long planReservationId, String expectedStatus) throws Exception {
        return awaitReservation(
                "finance reservation for plan reservation " + planReservationId + " to become " + expectedStatus,
                () -> {
                    JsonNode response = sendJsonRequest(
                            "GET",
                            financeBaseUrl + "/api/saga-reservations/plan/" + planId,
                            null,
                            null,
                            200
                    );
                    return findReservationById(response, "planReservationId", planReservationId)
                            .filter(node -> expectedStatus.equals(node.path("status").asText()));
                }
        );
    }

    private JsonNode awaitReservation(String description, ReservationSupplier supplier) throws Exception {
        Instant deadline = Instant.now().plus(POLL_TIMEOUT);

        while (Instant.now().isBefore(deadline)) {
            Optional<JsonNode> reservation = supplier.get();
            if (reservation.isPresent()) {
                return reservation.get();
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }

        throw new AssertionError("Timed out waiting for " + description);
    }

    private Optional<JsonNode> findReservationById(JsonNode arrayNode, String fieldName, long expectedValue) {
        if (!arrayNode.isArray()) {
            return Optional.empty();
        }

        for (JsonNode node : arrayNode) {
            if (node.path(fieldName).asLong() == expectedValue) {
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }

    private JsonNode sendJsonRequest(String method, String url, String body, String authorizationHeader,
                                     int expectedStatus) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10));

        if ("GET".equalsIgnoreCase(method)) {
            requestBuilder.GET();
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
            requestBuilder.header("Content-Type", "application/json");
        }

        if (authorizationHeader != null) {
            requestBuilder.header("Authorization", authorizationHeader);
        }

        HttpResponse<String> response = httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode())
                .as("Unexpected response for %s %s: %s", method, url, response.body())
                .isEqualTo(expectedStatus);

        if (response.body() == null || response.body().isBlank()) {
            return objectMapper.nullNode();
        }

        return objectMapper.readTree(response.body());
    }

    private String generateJwtToken(long userId, String role) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

        return Jwts.builder()
                .claim("role", role)
                .setSubject(Long.toString(userId))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @FunctionalInterface
    private interface ReservationSupplier {
        Optional<JsonNode> get() throws Exception;
    }
}
