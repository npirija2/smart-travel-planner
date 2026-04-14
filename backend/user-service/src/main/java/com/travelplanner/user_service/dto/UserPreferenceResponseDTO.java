package com.travelplanner.user_service.dto;

import lombok.Data;

@Data
public class UserPreferenceResponseDTO {
    private Integer id;
    private Integer userId;
    private String preferenceType;
    private String preferenceValue;
}
