package com.travelplanner.communication_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class NotificationRequestDTO {

    @NotBlank(message = "message must not be blank")
    private String message;

    @NotNull(message = "date is required")
    private LocalDateTime date;

    @NotNull(message = "userId is required")
    private Integer userId;

    @NotNull(message = "planId is required")
    private Integer planId;

    @NotBlank(message = "type must not be blank")
    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}