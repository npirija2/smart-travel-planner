package com.travelplanner.communication_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SharedLinkRequestDTO {

    @NotBlank(message = "url must not be blank")
    private String url;

    @NotNull(message = "planId is required")
    private Integer planId;

    @NotBlank(message = "type must not be blank")
    private String type;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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