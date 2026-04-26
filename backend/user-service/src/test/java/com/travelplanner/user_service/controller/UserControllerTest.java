package com.travelplanner.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.dto.UserRequestDTO;
import com.travelplanner.user_service.dto.UserResponseDTO;
import com.travelplanner.user_service.exception.GlobalExceptionHandler;
import com.travelplanner.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
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

        UserResponseDTO response = UserResponseDTO.builder()
                .id(1)
                .username("nejra")
                .email("nejra@test.com")
                .build();

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("nejra"))
                .andExpect(jsonPath("$.email").value("nejra@test.com"));
    }

    @Test
    void whenValidUpdate_thenReturns200() throws Exception {
        UserRequestDTO request = new UserRequestDTO();
        request.setUsername("nejra-updated");
        request.setEmail("nejra.updated@test.com");
        request.setPassword("newpassword123");

        UserResponseDTO response = UserResponseDTO.builder()
                .id(1)
                .username("nejra-updated")
                .email("nejra.updated@test.com")
                .build();

        when(userService.updateUser(eq(1), any(UserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nejra-updated"))
                .andExpect(jsonPath("$.email").value("nejra.updated@test.com"));
    }

    @Test
    void whenDelete_thenReturns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
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
