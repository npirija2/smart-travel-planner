package com.travelplanner.communication_service.dto;

import lombok.Data;

@Data
public class ReviewResponseDTO {
    private Integer id;
    private Integer userId;
    private Integer activityId;
    private Integer rating;
    private String comment;
}
