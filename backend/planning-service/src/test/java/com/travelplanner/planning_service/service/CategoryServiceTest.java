package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.CategoryRequestDTO;
import com.travelplanner.planning_service.dto.CategoryResponseDTO;
import com.travelplanner.planning_service.model.Category;
import com.travelplanner.planning_service.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @InjectMocks
    private CategoryService service;

    @Test
    void shouldCreateCategoryAndReturnMappedDTO() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Adventure");

        Category saved = Category.builder().id(1L).name("Adventure").build();
        when(repository.save(any(Category.class))).thenReturn(saved);

        CategoryResponseDTO result = service.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Adventure");
    }

    @Test
    void shouldSaveCategoryWithCorrectName() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Culture");

        Category saved = Category.builder().id(2L).name("Culture").build();
        when(repository.save(any(Category.class))).thenReturn(saved);

        service.create(dto);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Culture");
    }

    @Test
    void shouldReturnAllCategoriesMapped() {
        List<Category> categories = List.of(
                Category.builder().id(1L).name("Adventure").build(),
                Category.builder().id(2L).name("Culture").build()
        );
        when(repository.findAll()).thenReturn(categories);

        List<CategoryResponseDTO> result = service.getAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Adventure");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Culture");
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() {
        when(repository.findAll()).thenReturn(List.of());

        List<CategoryResponseDTO> result = service.getAll();

        assertThat(result).isEmpty();
    }
}