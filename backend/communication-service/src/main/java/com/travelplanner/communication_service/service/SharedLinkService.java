package com.travelplanner.communication_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.travelplanner.communication_service.dto.SharedLinkRequestDTO;
import com.travelplanner.communication_service.dto.SharedLinkResponseDTO;
import com.travelplanner.communication_service.exception.ResourceNotFoundException; // DODANO
import com.travelplanner.communication_service.model.SharedLink;
import com.travelplanner.communication_service.repository.SharedLinkRepository;
import com.travelplanner.communication_service.util.JwtUtils;

@Service
public class SharedLinkService {

    private final SharedLinkRepository sharedLinkRepository;
    private final JwtUtils jwtUtils; 

    public SharedLinkService(SharedLinkRepository sharedLinkRepository, JwtUtils jwtUtils) { // AŽURIRAN KONSTRUKTOR
        this.sharedLinkRepository = sharedLinkRepository;
        this.jwtUtils = jwtUtils;
    }

    public SharedLinkResponseDTO createSharedLink(SharedLinkRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader); 

        SharedLink sharedLink = new SharedLink();
        sharedLink.setUrl(requestDTO.getUrl());
        sharedLink.setPlanId(requestDTO.getPlanId());
        sharedLink.setType(requestDTO.getType());

        SharedLink saved = sharedLinkRepository.save(sharedLink);
        return mapToResponseDTO(saved);
    }

    public List<SharedLinkResponseDTO> getAllSharedLinks(String authHeader) {
        validateToken(authHeader); 
        return sharedLinkRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public SharedLinkResponseDTO getSharedLinkById(int id, String authHeader) {
        validateToken(authHeader); 
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));
        return mapToResponseDTO(sharedLink);
    }

    public List<SharedLinkResponseDTO> getSharedLinksByPlanId(int planId, String authHeader) {
        validateToken(authHeader); 
        return sharedLinkRepository.findByPlanId(planId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public SharedLinkResponseDTO updateSharedLink(int id, SharedLinkRequestDTO requestDTO, String authHeader) {
        validateToken(authHeader); 
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));

        sharedLink.setUrl(requestDTO.getUrl());
        sharedLink.setPlanId(requestDTO.getPlanId());
        sharedLink.setType(requestDTO.getType());

        SharedLink updated = sharedLinkRepository.save(sharedLink);
        return mapToResponseDTO(updated);
    }

    public void deleteSharedLink(int id, String authHeader) {
        validateToken(authHeader); 
        SharedLink sharedLink = sharedLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shared link not found with id " + id));
        sharedLinkRepository.delete(sharedLink);
    }

    private void validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid or missing Authorization header");
        }
        String token = authHeader.substring(7);
        jwtUtils.getClaims(token); // Validira potpis i datum isteka
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