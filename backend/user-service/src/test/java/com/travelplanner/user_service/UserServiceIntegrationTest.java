package com.travelplanner.user_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateReadUpdateAndDeleteUserThroughHttpLayer() throws Exception {
        String createRequest = """
                {
                  "username": "nejra",
                  "email": "nejra@example.com",
                  "password": "password123"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("nejra"))
                .andExpect(jsonPath("$.email").value("nejra@example.com"))
                .andReturn();

        JsonNode createdUser = objectMapper.readTree(createResult.getResponse().getContentAsString());
        int userId = createdUser.get("id").asInt();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findById(userId)).isPresent();
        assertThat(userRepository.findById(userId).orElseThrow().getPasswordHash()).isNotEqualTo("password123");

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("nejra"));

        String updateRequest = """
                {
                  "username": "nejra-updated",
                  "email": "nejra.updated@example.com",
                  "password": "newpass123"
                }
                """;

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("nejra-updated"))
                .andExpect(jsonPath("$.email").value("nejra.updated@example.com"));

        assertThat(userRepository.findById(userId)).isPresent();
        assertThat(userRepository.findById(userId).orElseThrow().getUsername()).isEqualTo("nejra-updated");

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    void shouldReturnStructuredJsonWhenCreateUserValidationFails() throws Exception {
        String invalidRequest = """
                {
                  "username": "",
                  "email": "not-an-email",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserWithDuplicateEmail() throws Exception {
        String firstRequest = """
                {
                  "username": "nejra",
                  "email": "nejra@example.com",
                  "password": "password123"
                }
                """;

        String duplicateRequest = """
                {
                  "username": "nejra-two",
                  "email": "nejra@example.com",
                  "password": "password456"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("Email is already registered"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
