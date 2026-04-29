package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.ActivityCategoryRequestDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.ActivityCategory;
import com.travelplanner.planning_service.model.Category;
import com.travelplanner.planning_service.repository.ActivityCategoryRepository;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityCategoryServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ActivityCategoryRepository repository;

    @InjectMocks
    private ActivityCategoryService service;

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldAssignCategoryToActivity() {
        // given
        ActivityCategoryRequestDTO dto = new ActivityCategoryRequestDTO();
        dto.setActivityId(1L);
        dto.setCategoryId(2L);

        Activity activity = Activity.builder().id(1L).build();
        Category category = Category.builder().id(2L).build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        // when
        service.assign(dto);

        ArgumentCaptor<ActivityCategory> captor = ArgumentCaptor.forClass(ActivityCategory.class);
        verify(repository).save(captor.capture());

        ActivityCategory saved = captor.getValue();
        assertThat(saved.getActivity()).isEqualTo(activity);
        assertThat(saved.getCategory()).isEqualTo(category);
    }

    @Test
    void shouldThrowWhenActivityNotFound() {
        ActivityCategoryRequestDTO dto = new ActivityCategoryRequestDTO();
        dto.setActivityId(99L);
        dto.setCategoryId(2L);

        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Activity not found");

        verifyNoInteractions(categoryRepository, repository);
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        // given
        ActivityCategoryRequestDTO dto = new ActivityCategoryRequestDTO();
        dto.setActivityId(1L);
        dto.setCategoryId(99L);

        Activity activity = Activity.builder().id(1L).build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assign(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");

        verifyNoInteractions(repository);
    }
}