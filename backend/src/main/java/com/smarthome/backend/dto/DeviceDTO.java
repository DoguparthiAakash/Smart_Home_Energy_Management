package com.smarthome.backend.dto;

import lombok.Data;

@Data
public class DeviceDTO {
    private Long id;
    private String name;
    private String type;
    private Double powerRating;
    private Boolean status;
    private String priority; // HIGH, MEDIUM, LOW
    private String location;
    private Double powerLimit;
    private String firmwareVersion;
    private String healthStatus;
    private String customIcon;
}
