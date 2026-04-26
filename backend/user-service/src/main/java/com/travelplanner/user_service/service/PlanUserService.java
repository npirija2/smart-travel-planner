package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.PlanUserRequestDTO;
import com.travelplanner.user_service.dto.PlanUserResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.model.PlanUser;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.mapper.PlanUserMapper;
import com.travelplanner.user_service.repository.PlanUserRepository;
import com.travelplanner.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanUserService {

    private final UserRepository userRepository;
    private final PlanUserRepository planUserRepository;
    private final PlanUserMapper planUserMapper;

    @Transactional
    public PlanUserResponseDTO createPlanUser(Integer userId, PlanUserRequestDTO request) {
        User user = findUser(userId);

        PlanUser planUser = planUserMapper.toEntity(request);
        planUser.setUser(user);

        return planUserMapper.toResponseDTO(planUserRepository.save(planUser));
    }

    @Transactional(readOnly = true)
    public PlanUserResponseDTO getPlanUserById(Integer planUserId) {
        return planUserMapper.toResponseDTO(findPlanUser(planUserId));
    }

    @Transactional(readOnly = true)
    public List<PlanUserResponseDTO> getPlanUsersByUserId(Integer userId) {
        return planUserRepository.findAll().stream()
                .filter(planUser -> planUser.getUser() != null && planUser.getUser().getId().equals(userId))
                .map(planUserMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlanUserResponseDTO updatePlanUser(Integer planUserId, PlanUserRequestDTO request) {
        PlanUser planUser = findPlanUser(planUserId);
        planUserMapper.updateEntityFromDTO(request, planUser);
        return planUserMapper.toResponseDTO(planUserRepository.save(planUser));
    }

    @Transactional
    public void deletePlanUser(Integer planUserId) {
        planUserRepository.delete(findPlanUser(planUserId));
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + userId + " nije pronađen"));
    }

    private PlanUser findPlanUser(Integer planUserId) {
        return planUserRepository.findById(planUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan korisnik sa ID-jem " + planUserId + " nije pronađen"));
    }
}
