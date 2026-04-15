package com.travelplanner.planning_service.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationResponseDTO {

    private Long id;
    private String name;
    private Long destinationId;
    private String destinationName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String type;
}