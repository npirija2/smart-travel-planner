package com.travelplanner.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPreferenceRequestDTO {

    @NotBlank(message = "Preference type is required")
    private String preferenceType;

    @NotBlank(message = "Preference value is required")
    private String preferenceValue;
}
