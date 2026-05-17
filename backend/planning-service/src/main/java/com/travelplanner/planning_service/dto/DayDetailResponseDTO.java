package com.travelplanner.planning_service.dto;

import java.time.LocalDate;
import java.util.List;

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
public class DayDetailResponseDTO {

    private Long id;
    private LocalDate date;
    private Long travelPlanId;
    private List<ActivityResponseDTO> activities;
}
