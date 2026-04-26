package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.UserPreferenceRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.mapper.UserPreferenceMapper;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.model.UserPreference;
import com.travelplanner.user_service.repository.UserPreferenceRepository;
import com.travelplanner.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceMapper userPreferenceMapper;

    @Transactional
    public UserPreferenceResponseDTO createPreference(Integer userId, UserPreferenceRequestDTO request) {
        User user = findUser(userId);

        UserPreference preference = userPreferenceMapper.toEntity(request);
        preference.setUser(user);

        return userPreferenceMapper.toResponseDTO(userPreferenceRepository.save(preference));
    }

    @Transactional
    public List<UserPreferenceResponseDTO> createPreferences(Integer userId, List<UserPreferenceRequestDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Preferences list cannot be empty");
        }

        User user = findUser(userId);

        List<UserPreference> preferences = requests.stream()
                .map(userPreferenceMapper::toEntity)
                .peek(preference -> preference.setUser(user))
                .collect(Collectors.toList());

        return userPreferenceRepository.saveAll(preferences).stream()
                .map(userPreferenceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponseDTO getPreferenceById(Integer preferenceId) {
        return userPreferenceMapper.toResponseDTO(findPreference(preferenceId));
    }

    @Transactional(readOnly = true)
    public List<UserPreferenceResponseDTO> getPreferencesByUserId(Integer userId) {
        return userPreferenceRepository.findAll().stream()
                .filter(preference -> preference.getUser() != null && preference.getUser().getId().equals(userId))
                .map(userPreferenceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserPreferenceResponseDTO updatePreference(Integer preferenceId, UserPreferenceRequestDTO request) {
        UserPreference preference = findPreference(preferenceId);
        userPreferenceMapper.updateEntityFromDTO(request, preference);
        return userPreferenceMapper.toResponseDTO(userPreferenceRepository.save(preference));
    }

    @Transactional
    public void deletePreference(Integer preferenceId) {
        userPreferenceRepository.delete(findPreference(preferenceId));
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa ID-jem " + userId + " nije pronađen"));
    }

    private UserPreference findPreference(Integer preferenceId) {
        return userPreferenceRepository.findById(preferenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Preference sa ID-jem " + preferenceId + " nije pronađena"));
    }
}
