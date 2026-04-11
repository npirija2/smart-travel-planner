package com.travelplanner.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.service.UserService;
import com.travelplanner.user_service.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class) // Importujemo handler da bi test prepoznao format greške
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenValidInput_thenReturns201() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("nejra");
        request.setEmail("nejra@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void whenInvalidInput_thenReturns400AndCustomError() throws Exception {
        UserRequestDTO invalidRequest = new UserRequestDTO();
        invalidRequest.setUsername(""); 
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("123");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.message").exists());
    }
}