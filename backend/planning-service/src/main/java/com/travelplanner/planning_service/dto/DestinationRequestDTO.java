package com.travelplanner.planning_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;
}