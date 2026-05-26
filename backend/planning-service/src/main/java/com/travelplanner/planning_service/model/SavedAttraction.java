package com.travelplanner.planning_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "saved_attractions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"plan_id", "location_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedAttraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}