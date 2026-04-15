package com.travelplanner.planning_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Long dayId;
    private LocalDate dayDate;
    private Long createdBy;
    private Long locationId;
    private String locationName;
    private String timeslot;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private String status;
}