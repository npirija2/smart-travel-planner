package com.travelplanner.planning_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAttractionToItineraryRequest {
    private Long locationId;
    private Long dayId;
}