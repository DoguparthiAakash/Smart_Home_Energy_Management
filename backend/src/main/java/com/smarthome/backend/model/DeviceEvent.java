package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_events")
public class DeviceEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private String eventType; // e.g., "TURNED_ON", "TURNED_OFF", "LOAD_SHED"

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private String description;
}
