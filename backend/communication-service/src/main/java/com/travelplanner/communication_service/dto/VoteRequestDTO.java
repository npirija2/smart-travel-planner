package com.travelplanner.communication_service.dto;

import jakarta.validation.constraints.NotNull;

public class VoteRequestDTO {

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotNull(message = "activityId is required")
    private Integer activityId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }
}