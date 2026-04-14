package com.travelplanner.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanUserRequestDTO {

    @NotNull(message = "Plan ID is required")
    private Integer planId;

    @NotBlank(message = "Role is required")
    private String role;
}
