package com.travelplanner.planning_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Destination id is required")
    private Long destinationId;

    @NotBlank(message = "Address is required")
    private String address;

    private Double latitude;

    private Double longitude;

    @NotBlank(message = "Type is required")
    private String type;
}