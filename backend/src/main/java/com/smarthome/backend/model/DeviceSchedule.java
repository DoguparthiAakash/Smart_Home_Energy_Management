package com.smarthome.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_schedules")
public class DeviceSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "scheduled_on_time")
    private LocalDateTime scheduledOnTime;

    @Column(name = "scheduled_off_time")
    private LocalDateTime scheduledOffTime;

    @Column(nullable = false)
    private Boolean active = true;
}
