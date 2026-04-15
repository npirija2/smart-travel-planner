package com.travelplanner.communication_service.dto;

import lombok.Data;

@Data
public class VoteResponseDTO {
    
    private Integer id;
    private Integer userId;
    private Integer activityId;
}