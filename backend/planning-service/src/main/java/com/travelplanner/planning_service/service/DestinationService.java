package com.travelplanner.planning_service.service;

import com.travelplanner.planning_service.dto.*;
import com.travelplanner.planning_service.exception.ResourceNotFoundException;
import com.travelplanner.planning_service.model.Destination;
import com.travelplanner.planning_service.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private final DestinationRepository repository;

    public List<DestinationResponseDTO> getAll() {
        return repository.findAll().stream().map(this::map).toList();
    }

    public DestinationResponseDTO getById(Long id) {
        Destination d = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destination not found"));
        return map(d);
    }

    public DestinationResponseDTO create(DestinationRequestDTO dto) {
        Destination d = Destination.builder().name(dto.getName()).build();
        return map(repository.save(d));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private DestinationResponseDTO map(Destination d) {
        return DestinationResponseDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .build();
    }
}