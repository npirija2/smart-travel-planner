package com.travelplanner.communication_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SharedLinkRequestDTO {

    @NotBlank(message = "URL is required")
    private String url;

    @NotNull(message = "Plan ID is required")
    private Integer planId;

    @NotBlank(message = "Type is required")
    private String type;
}
