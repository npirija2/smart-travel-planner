package com.travelplanner.planning_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.repository.DestinationRepository;
import com.travelplanner.planning_service.repository.TravelPlanRepository;
import com.travelplanner.shared.security.JwtValidator;

import io.jsonwebtoken.Claims;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TravelPlanIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @MockBean
    private JwtValidator jwtUtils;

    private Destination destination;

    @BeforeEach
    void setUp() {
        travelPlanRepository.deleteAll();
        destinationRepository.deleteAll();

        destination = destinationRepository.save(Destination.builder()
                .name("Paris")
                .build());

        Claims claims = mock(Claims.class);
        when(jwtUtils.getClaims("test-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("42");
        when(claims.get("role")).thenReturn("USER");
    }

    @Test
    void shouldCreateAndFetchTravelPlanForAuthenticatedOwner() throws Exception {
        String request = """
                {
                  "name": "Summer in Paris",
                  "startDate": "2026-07-01",
                  "endDate": "2026-07-10",
                  "destinationId": %d,
                  "description": "Ten day city break",
                  "status": "PLANNED"
                }
                """.formatted(destination.getId());

        MvcResult createResult = mockMvc.perform(post("/api/travel-plans")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Summer in Paris"))
                .andExpect(jsonPath("$.ownerId").value(42))
                .andExpect(jsonPath("$.destinationName").value("Paris"))
                .andReturn();

        JsonNode createdPlan = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long planId = createdPlan.get("id").asLong();

        assertThat(travelPlanRepository.count()).isEqualTo(1);
        assertThat(travelPlanRepository.findById(planId)).isPresent();

        mockMvc.perform(get("/api/travel-plans")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(planId))
                .andExpect(jsonPath("$[0].ownerId").value(42))
                .andExpect(jsonPath("$[0].destinationName").value("Paris"));
    }

    @Test
    void shouldReturnValidationErrorWhenTravelPlanDatesAreInvalid() throws Exception {
        String invalidRequest = """
                {
                  "name": "Broken trip",
                  "startDate": "2026-08-10",
                  "endDate": "2026-08-01",
                  "destinationId": %d,
                  "description": "Bad dates",
                  "status": "PLANNED"
                }
                """.formatted(destination.getId());

        mockMvc.perform(post("/api/travel-plans")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("End date must be after or equal to start date"));
    }
}
