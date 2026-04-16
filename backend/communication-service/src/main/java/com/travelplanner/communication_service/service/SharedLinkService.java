package com.travelplanner.communication_service.service;

import com.travelplanner.communication_service.dto.SharedLinkRequestDTO;
import com.travelplanner.communication_service.dto.SharedLinkResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException;
import com.travelplanner.communication_service.model.SharedLink;
import com.travelplanner.communication_service.repository.SharedLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SharedLinkService {

    private final SharedLinkRepository sharedLinkRepository;

    public SharedLinkService(SharedLinkRepository sharedLinkRepository) {
        this.sharedLinkRepository = sharedLinkRepository;
    }

    public SharedLinkResponseDTO createSharedLink(SharedLinkRequestDTO requestDTO) {
        SharedLink sharedLink = new SharedLink();
        sharedLink.setUrl(requestDTO.getUrl());
        sharedLink.setPlanId(requestDTO.getPlanId());
        sharedLink.setType(requestDTO.getType());

        SharedLink saved = sharedLinkRepository.save(sharedLink);
        return mapToResponseDTO(saved);
    }

    public List<SharedLinkResponseDTO> getAllSharedLinks() {
        return sharedLinkRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public SharedLinkResponseDTO getSharedLinkById(Integer id) {
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));
        return mapToResponseDTO(sharedLink);
    }

    public List<SharedLinkResponseDTO> getSharedLinksByPlanId(Integer planId) {
        return sharedLinkRepository.findByPlanId(planId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public SharedLinkResponseDTO updateSharedLink(Integer id, SharedLinkRequestDTO requestDTO) {
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));

        sharedLink.setUrl(requestDTO.getUrl());
        sharedLink.setPlanId(requestDTO.getPlanId());
        sharedLink.setType(requestDTO.getType());

        SharedLink updated = sharedLinkRepository.save(sharedLink);
        return mapToResponseDTO(updated);
    }

    public void deleteSharedLink(Integer id) {
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));
        sharedLinkRepository.delete(sharedLink);
    }

    private SharedLinkResponseDTO mapToResponseDTO(SharedLink sharedLink) {
        SharedLinkResponseDTO DTO = new SharedLinkResponseDTO();
        DTO.setId(sharedLink.getId());
        DTO.setUrl(sharedLink.getUrl());
        DTO.setPlanId(sharedLink.getPlanId());
        DTO.setType(sharedLink.getType());
        return DTO;
    }
}