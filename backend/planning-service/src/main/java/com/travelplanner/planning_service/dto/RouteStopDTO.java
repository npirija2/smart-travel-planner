package com.travelplanner.planning_service.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteStopDTO {

    private Integer suggestedOrder;
    private Long activityId;
    private String activityName;
    private Long dayId;
    private LocalDate dayDate;
    private String timeslot;
    private Long locationId;
    private String locationName;
    private Double latitude;
    private Double longitude;
    private String address;
}
