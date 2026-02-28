package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // e.g., "USER_REGISTRATION", "SECURITY", "SIMULATION", "SYSTEM"

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    public enum Severity {
        INFO, WARNING, ERROR, CRITICAL
    }
}
