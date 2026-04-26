package com.travelplanner.user_service.mapper;

import com.travelplanner.user_service.dto.UserPreferenceRequestDTO;
import com.travelplanner.user_service.dto.UserPreferenceResponseDTO;
import com.travelplanner.user_service.model.UserPreference;
import org.springframework.stereotype.Component;

@Component
public class UserPreferenceMapper {

    public UserPreferenceResponseDTO toResponseDTO(UserPreference preference) {
        if (preference == null) {
            return null;
        }

        UserPreferenceResponseDTO dto = new UserPreferenceResponseDTO();
        dto.setId(preference.getId());
        dto.setUserId(preference.getUser() != null ? preference.getUser().getId() : null);
        dto.setPreferenceType(preference.getPreferenceType());
        dto.setPreferenceValue(preference.getPreferenceValue());
        return dto;
    }

    public UserPreference toEntity(UserPreferenceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return UserPreference.builder()
                .preferenceType(dto.getPreferenceType())
                .preferenceValue(dto.getPreferenceValue())
                .build();
    }

    public void updateEntityFromDTO(UserPreferenceRequestDTO dto, UserPreference preference) {
        if (dto == null || preference == null) {
            return;
        }

        preference.setPreferenceType(dto.getPreferenceType());
        preference.setPreferenceValue(dto.getPreferenceValue());
    }
}
