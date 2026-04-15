package com.travelplanner.communication_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Integer id;
    private String message;
    private LocalDateTime date;
    private Integer userId;
    private Integer planId;
    private String type;
}
