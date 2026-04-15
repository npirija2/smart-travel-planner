package com.travelplanner.communication_service.dto;

import lombok.Data;

@Data
public class SharedLinkResponseDTO {
    private Integer id;
    private String url;
    private Integer planId;
    private String type;
}
