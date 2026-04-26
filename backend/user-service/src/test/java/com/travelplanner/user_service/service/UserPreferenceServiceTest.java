package com.travelplanner.user_service.service;

import com.travelplanner.user_service.dto.UserPreferenceRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceResponseDTO;
import com.travelplanner.user_service.exception.ResourceNotFoundException;
import com.travelplanner.user_service.mapper.UserPreferenceMapper;
import com.travelplanner.user_service.model.User;
import com.travelplanner.user_service.model.UserPreference;
import com.travelplanner.user_service.repository.UserPreferenceRepository;
import com.travelplanner.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserPreferenceMapper userPreferenceMapper;

    @InjectMocks
    private UserPreferenceService userPreferenceService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1).username("nejra").email("nejra@test.com").build();
    }

    @Test
    void createPreference_success() {
        UserPreferenceRequestDTO request = new UserPreferenceRequestDTO();
        request.setPreferenceType("language");
        request.setPreferenceValue("en");

        UserPreference preference = UserPreference.builder()
                .id(10)
                .user(user)
                .preferenceType("language")
                .preferenceValue("en")
                .build();

        UserPreferenceResponseDTO mappedResponse = new UserPreferenceResponseDTO();
        mappedResponse.setId(10);
        mappedResponse.setUserId(1);
        mappedResponse.setPreferenceType("language");
        mappedResponse.setPreferenceValue("en");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userPreferenceMapper.toEntity(request)).thenReturn(UserPreference.builder()
                .preferenceType("language")
                .preferenceValue("en")
                .build());
        when(userPreferenceRepository.save(any())).thenReturn(preference);
        when(userPreferenceMapper.toResponseDTO(any(UserPreference.class))).thenReturn(mappedResponse);

        UserPreferenceResponseDTO response = userPreferenceService.createPreference(1, request);

        assertNotNull(response);
        assertEquals(10, response.getId());
        assertEquals(1, response.getUserId());
    }

    @Test
    void createPreferences_success() {
        UserPreferenceRequestDTO request1 = new UserPreferenceRequestDTO();
        request1.setPreferenceType("language");
        request1.setPreferenceValue("en");

        UserPreferenceRequestDTO request2 = new UserPreferenceRequestDTO();
        request2.setPreferenceType("currency");
        request2.setPreferenceValue("eur");

        UserPreference preference1 = UserPreference.builder()
                .id(10)
                .user(user)
                .preferenceType("language")
                .preferenceValue("en")
                .build();

        UserPreference preference2 = UserPreference.builder()
                .id(11)
                .user(user)
                .preferenceType("currency")
                .preferenceValue("eur")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userPreferenceMapper.toEntity(request1)).thenReturn(UserPreference.builder()
                .preferenceType("language")
                .preferenceValue("en")
                .build());
        when(userPreferenceMapper.toEntity(request2)).thenReturn(UserPreference.builder()
                .preferenceType("currency")
                .preferenceValue("eur")
                .build());
        when(userPreferenceRepository.saveAll(any())).thenReturn(List.of(preference1, preference2));

        UserPreferenceResponseDTO response1 = new UserPreferenceResponseDTO();
        response1.setId(10);
        response1.setUserId(1);
        response1.setPreferenceType("language");
        response1.setPreferenceValue("en");

        UserPreferenceResponseDTO response2 = new UserPreferenceResponseDTO();
        response2.setId(11);
        response2.setUserId(1);
        response2.setPreferenceType("currency");
        response2.setPreferenceValue("eur");

        when(userPreferenceMapper.toResponseDTO(preference1)).thenReturn(response1);
        when(userPreferenceMapper.toResponseDTO(preference2)).thenReturn(response2);

        List<UserPreferenceResponseDTO> response = userPreferenceService.createPreferences(1, List.of(request1, request2));

        assertEquals(2, response.size());
        assertEquals("language", response.get(0).getPreferenceType());
        assertEquals("currency", response.get(1).getPreferenceType());
    }

    @Test
    void createPreferences_emptyList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userPreferenceService.createPreferences(1, List.of()));
    }

    @Test
    void updatePreference_success() {
        UserPreference existing = UserPreference.builder()
                .id(10)
                .user(user)
                .preferenceType("language")
                .preferenceValue("en")
                .build();

        when(userPreferenceRepository.findById(10)).thenReturn(Optional.of(existing));
        when(userPreferenceRepository.save(any())).thenReturn(existing);

        UserPreferenceResponseDTO mappedResponse = new UserPreferenceResponseDTO();
        mappedResponse.setId(10);
        mappedResponse.setUserId(1);
        mappedResponse.setPreferenceType("currency");
        mappedResponse.setPreferenceValue("eur");
        when(userPreferenceMapper.toResponseDTO(any(UserPreference.class))).thenReturn(mappedResponse);

        UserPreferenceRequestDTO request = new UserPreferenceRequestDTO();
        request.setPreferenceType("currency");
        request.setPreferenceValue("eur");

        UserPreferenceResponseDTO response = userPreferenceService.updatePreference(10, request);

        assertNotNull(response);
        assertEquals("currency", response.getPreferenceType());
        assertEquals("eur", response.getPreferenceValue());
    }

    @Test
    void deletePreference_success() {
        UserPreference existing = UserPreference.builder()
                .id(10)
                .user(user)
                .preferenceType("language")
                .preferenceValue("en")
                .build();

        when(userPreferenceRepository.findById(10)).thenReturn(Optional.of(existing));

        userPreferenceService.deletePreference(10);

        verify(userPreferenceRepository).delete(existing);
    }

    @Test
    void getPreferenceById_notFound() {
        when(userPreferenceRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userPreferenceService.getPreferenceById(10));
    }
}
