package com.travelplanner.user_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserPreferenceBatchRequestDTO {

    @NotEmpty(message = "Preferences list cannot be empty")
    @Valid
    private List<UserPreferenceRequestDTO> preferences;
}
