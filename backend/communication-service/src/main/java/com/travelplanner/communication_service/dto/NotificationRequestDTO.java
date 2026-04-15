package com.travelplanner.communication_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequestDTO {

    @NotBlank(message = "Message is required")
    private String message;

    private LocalDateTime date;

    @NotNull(message = "User ID is required")
    private Integer userId;

    private Integer planId;

    @NotBlank(message = "Type is required")
    private String type;
}
