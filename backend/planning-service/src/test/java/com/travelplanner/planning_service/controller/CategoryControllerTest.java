package com.travelplanner.planning_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelplanner.planning_service.dto.CategoryRequestDTO;
import com.travelplanner.planning_service.dto.CategoryResponseDTO;
import com.travelplanner.planning_service.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // PAZI NA OVAJ IMPORT
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryRequestDTO requestDTO;
    private CategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Koristimo Builder jer ga imaš u DTO-u
        requestDTO = CategoryRequestDTO.builder()
                .name("Museums")
                .build();

        responseDTO = CategoryResponseDTO.builder()
                .id(1L)
                .name("Museums")
                .build();
    }

    @Test
    void shouldCreateCategory() throws Exception {
        when(categoryService.create(any(CategoryRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Provera za 201 status
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Museums"));
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Museums"));
    }

    @Test
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {
        // Testiramo @Valid validaciju iz kontrolera
        CategoryRequestDTO invalidRequest = CategoryRequestDTO.builder()
                .name("") // Prazno ime jer imaš @NotBlank
                .build();

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
