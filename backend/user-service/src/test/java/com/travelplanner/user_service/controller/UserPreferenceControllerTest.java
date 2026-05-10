package com.travelplanner.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.user_service.dto.UserPreferenceBatchRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceResponseDTO;
import com.travelplanner.user_service.exception.GlobalExceptionHandler;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPreferenceController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @Test
    void createPreference_returns201() throws Exception {
        UserPreferenceRequestDTO request = new UserPreferenceRequestDTO();
        request.setPreferenceType("language");
        request.setPreferenceValue("en");

        UserPreferenceResponseDTO response = new UserPreferenceResponseDTO();
        response.setId(5);
        response.setUserId(1);
        response.setPreferenceType("language");
        response.setPreferenceValue("en");

        when(userPreferenceService.createPreference(eq(1), any(UserPreferenceRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/1/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createPreferencesBatch_returns201() throws Exception {
        UserPreferenceRequestDTO first = new UserPreferenceRequestDTO();
        first.setPreferenceType("language");
        first.setPreferenceValue("en");

        UserPreferenceRequestDTO second = new UserPreferenceRequestDTO();
        second.setPreferenceType("currency");
        second.setPreferenceValue("eur");

        UserPreferenceBatchRequestDTO request = new UserPreferenceBatchRequestDTO();
        request.setPreferences(List.of(first, second));

        UserPreferenceResponseDTO response1 = new UserPreferenceResponseDTO();
        response1.setId(5);
        response1.setUserId(1);
        response1.setPreferenceType("language");
        response1.setPreferenceValue("en");

        UserPreferenceResponseDTO response2 = new UserPreferenceResponseDTO();
        response2.setId(6);
        response2.setUserId(1);
        response2.setPreferenceType("currency");
        response2.setPreferenceValue("eur");

        when(userPreferenceService.createPreferences(eq(1), anyList())).thenReturn(List.of(response1, response2));

        mockMvc.perform(post("/api/users/1/preferences/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[1].id").value(6));
    }

    @Test
    void getPreferencesByUser_returns200() throws Exception {
        UserPreferenceResponseDTO response = new UserPreferenceResponseDTO();
        response.setId(5);
        response.setUserId(1);
        response.setPreferenceType("language");
        response.setPreferenceValue("en");

        when(userPreferenceService.getPreferencesByUserId(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/users/1/preferences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].preferenceType").value("language"));
    }

    @Test
    void getPreferenceById_whenMissing_returns404() throws Exception {
        when(userPreferenceService.getPreferenceById(5))
                .thenThrow(new ResourceNotFoundException("Preference sa ID-jem 5 nije pronađena"));

        mockMvc.perform(get("/api/preferences/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updatePreference_returns200() throws Exception {
        UserPreferenceRequestDTO request = new UserPreferenceRequestDTO();
        request.setPreferenceType("currency");
        request.setPreferenceValue("eur");

        UserPreferenceResponseDTO response = new UserPreferenceResponseDTO();
        response.setId(5);
        response.setUserId(1);
        response.setPreferenceType("currency");
        response.setPreferenceValue("eur");

        when(userPreferenceService.updatePreference(eq(5), any(UserPreferenceRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/api/preferences/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferenceValue").value("eur"));
    }

    @Test
    void deletePreference_returns204() throws Exception {
        mockMvc.perform(delete("/api/preferences/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createPreference_withInvalidPayload_returns400() throws Exception {
        UserPreferenceRequestDTO request = new UserPreferenceRequestDTO();
        request.setPreferenceType("");
        request.setPreferenceValue("");

        mockMvc.perform(post("/api/users/1/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createPreferencesBatch_withEmptyList_returns400() throws Exception {
        UserPreferenceBatchRequestDTO request = new UserPreferenceBatchRequestDTO();
        request.setPreferences(List.of());

        mockMvc.perform(post("/api/users/1/preferences/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
