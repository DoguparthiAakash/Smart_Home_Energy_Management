package com.smarthome.backend.controller;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.dto.DeviceScheduleDTO;
import com.smarthome.backend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public List<DeviceDTO> getDevices(Authentication authentication) {
        return deviceService.getDevicesForUser(authentication.getName());
    }

    @PostMapping
    public DeviceDTO addDevice(@RequestBody DeviceDTO deviceDTO, Authentication authentication) {
        return deviceService.addDevice(authentication.getName(), deviceDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDevice(@PathVariable Long id, @RequestBody DeviceDTO deviceDTO) {
        try {
            DeviceDTO updated = deviceService.updateDevice(id, deviceDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> toggleDevice(@PathVariable Long id) {
        try {
            DeviceDTO device = deviceService.toggleDevice(id);
            return ResponseEntity.ok(device);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<DeviceScheduleDTO> getDeviceSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getSchedule(id));
    }

    @PostMapping("/{id}/schedule")
    public ResponseEntity<?> saveDeviceSchedule(@PathVariable Long id, @RequestBody DeviceScheduleDTO dto) {
        deviceService.saveSchedule(id, dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/icon")
    public ResponseEntity<?> updateIcon(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String base64Icon = body.get("icon");
        DeviceDTO updated = deviceService.updateDeviceIcon(id, base64Icon);
        return ResponseEntity.ok(updated);
    }
}
