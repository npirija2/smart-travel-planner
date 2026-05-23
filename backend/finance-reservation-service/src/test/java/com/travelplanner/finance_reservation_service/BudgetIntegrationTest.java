package com.travelplanner.finance_reservation_service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.finance_reservation_service.repository.BudgetRepository;
import com.travelplanner.shared.security.JwtValidator;

import io.jsonwebtoken.Claims;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BudgetIntegrationTest {

    private static final String AUTH_HEADER = "Bearer test-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BudgetRepository budgetRepository;

    @MockBean
    private JwtValidator jwtUtils;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        when(jwtUtils.getClaims("test-token")).thenReturn(mock(Claims.class));
    }

    @Test
    void shouldCreateFetchAndFilterBudgetThroughFullStack() throws Exception {
        Long planId = 404L;
        String request = """
                {
                  "totalAmount": 2500.0,
                  "planId": %s,
                  "currency": "EUR"
                }
                """.formatted(planId);

        MvcResult createResult = mockMvc.perform(post("/api/budgets")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.planId").value(planId))
                .andExpect(jsonPath("$.totalAmount").value(2500.0))
                .andReturn();

        JsonNode createdBudget = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String budgetId = createdBudget.get("id").asText();

        assertThat(budgetRepository.count()).isEqualTo(1);
        assertThat(budgetRepository.findById(UUID.fromString(budgetId))).isPresent();

        mockMvc.perform(get("/api/budgets/{id}", budgetId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(budgetId))
                .andExpect(jsonPath("$.currency").value("EUR"));

        mockMvc.perform(get("/api/budgets/plan/{planId}", planId)
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(budgetId))
                .andExpect(jsonPath("$[0].planId").value(planId));
    }

    @Test
    void shouldReturnValidationErrorAsJsonForInvalidBudgetRequest() throws Exception {
        Long planId = 405L;
        String invalidRequest = """
                {
                  "totalAmount": -5,
                  "planId": %s,
                  "currency": "EUR"
                }
                """.formatted(planId);

        mockMvc.perform(post("/api/budgets")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}
