package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.LOW;

    @Column(name = "last_start_time")
    private LocalDateTime lastStartTime;

    @Column
    private String location; // e.g., "Bedroom", "Hall"

    @Column(name = "power_limit")
    private Double powerLimit;

    @Column(name = "firmware_version")
    private String firmwareVersion = "1.0.0";

    @Column(name = "health_status")
    private String healthStatus = "EXCELLENT";

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }
}
