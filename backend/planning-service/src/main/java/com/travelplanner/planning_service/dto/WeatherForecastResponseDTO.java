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
public class WeatherForecastResponseDTO {

    private LocalDate date;
    private String condition;
    private Integer temperatureCelsius;
    private String recommendation;
    private String suggestedTimeslot;
}
