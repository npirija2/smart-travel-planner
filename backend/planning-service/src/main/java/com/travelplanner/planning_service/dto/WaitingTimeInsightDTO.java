package com.travelplanner.planning_service.dto;

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
public class WaitingTimeInsightDTO {

    private Long activityId;
    private String activityName;
    private String locationName;
    private Integer expectedWaitMinutes;
    private String suggestedWindow;
    private String advice;
}
