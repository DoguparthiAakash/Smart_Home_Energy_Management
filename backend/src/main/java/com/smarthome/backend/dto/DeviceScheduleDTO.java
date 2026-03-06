package com.smarthome.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceScheduleDTO {
    private String onTime; // HH:mm format
    private boolean onEnabled;
    private String offTime; // HH:mm format
    private boolean offEnabled;
    private String timezone;
}
