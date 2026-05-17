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
public class DailyLoadDTO {

    private Long dayId;
    private LocalDate date;
    private Integer activityCount;
    private Integer totalDurationMinutes;
    private String intensity;
    private String warning;
}
