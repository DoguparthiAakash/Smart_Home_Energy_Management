package com.smarthome.backend.dto;

import lombok.Data;

@Data
public class DeviceDTO {
    private Long id;
    private String name;
    private String type;
    private Double powerRating;
    private Boolean status;
    private Boolean isCritical;
    private String mqttTopic;
}
