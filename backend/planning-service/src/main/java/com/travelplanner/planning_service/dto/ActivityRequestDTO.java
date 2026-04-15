package com.travelplanner.planning_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Day id is required")
    private Long dayId;

    private Long createdBy;

    @NotNull(message = "Location id is required")
    private Long locationId;

    private String timeslot;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private String status;
}