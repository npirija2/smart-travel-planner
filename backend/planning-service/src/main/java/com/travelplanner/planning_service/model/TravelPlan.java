package com.travelplanner.planning_service.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "travel_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String destination;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    private String status;

    @Column(name = "optimization_priority")
    private String optimizationPriority;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "share_token")
    private String shareToken;

    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Day> days;
}