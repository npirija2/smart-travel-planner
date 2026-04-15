package com.travelplanner.planning_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.travelplanner.planning_service.dto.CategoryRequestDTO;
import com.travelplanner.planning_service.dto.CategoryResponseDTO;
import com.travelplanner.planning_service.model.Category;
import com.travelplanner.planning_service.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        Category c = Category.builder().name(dto.getName()).build();
        return map(repository.save(c));
    }

    public List<CategoryResponseDTO> getAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    private CategoryResponseDTO map(Category c) {
        return CategoryResponseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .build();
    }
}