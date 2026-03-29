package com.travelplanner.planning_service.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String type;
    private Double lat;
    private Double lng;
    private String category;
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "opening_hours", columnDefinition = "TEXT")
    private String openingHours;

    @Column(name = "avg_visit_time")
    private Integer avgVisitTime;

    @Column(name = "avg_price_per_person")
    private Double avgPricePerPerson;
}