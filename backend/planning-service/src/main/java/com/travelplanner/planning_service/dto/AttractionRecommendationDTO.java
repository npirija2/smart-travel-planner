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
public class AttractionRecommendationDTO {
    
    private Long locationId;
    private String name;
    private String address;
    private String type;
    private String destinationName;
    private String reason;
    private Integer matchScore;
}
