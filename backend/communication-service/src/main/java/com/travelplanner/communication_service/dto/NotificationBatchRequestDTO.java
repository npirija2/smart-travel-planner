package com.travelplanner.communication_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class NotificationBatchRequestDTO {

    @NotEmpty(message = "notifications list must not be empty")
    @Valid
    private List<NotificationRequestDTO> notifications;

    public List<NotificationRequestDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationRequestDTO> notifications) {
        this.notifications = notifications;
    }
}
