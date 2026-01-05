package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // e.g., "AC", "LIGHT", "FAN"

    @Column(name = "power_rating")
    private Double powerRating; // In Watts

    @Column(nullable = false)
    private Boolean status = false; // false = OFF, true = ON
}
