package com.travelplanner.planning_service.service;

import org.springframework.stereotype.Service;

import com.travelplanner.planning_service.dto.ActivityCategoryRequestDTO;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Activity;
import com.travelplanner.planning_service.model.ActivityCategory;
import com.travelplanner.planning_service.model.Category;
import com.travelplanner.planning_service.repository.ActivityCategoryRepository;
import com.travelplanner.planning_service.repository.ActivityRepository;
import com.travelplanner.planning_service.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityCategoryService {

    private final ActivityRepository activityRepository;
    private final CategoryRepository categoryRepository;
    private final ActivityCategoryRepository repository;

    public void assign(ActivityCategoryRequestDTO dto) {

        Activity a = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found"));

        Category c = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        ActivityCategory ac = ActivityCategory.builder()
                .activity(a)
                .category(c)
                .build();

        repository.save(ac);
    }
}