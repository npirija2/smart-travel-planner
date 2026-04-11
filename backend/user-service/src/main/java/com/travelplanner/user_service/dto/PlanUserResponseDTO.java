package com.travelplanner.user_service.dto;

import lombok.Data;

@Data
public class PlanUserResponseDTO {
    private Integer id;
    private Integer userId;
    private Integer planId;
    private String role;
}
