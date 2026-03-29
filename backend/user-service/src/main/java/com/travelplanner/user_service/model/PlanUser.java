package com.travelplanner.user_service.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "plan_id", nullable = false)
    private Integer planId; 

    private String role; 
}
