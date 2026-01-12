package com.smarthome.backend.controller;

import com.smarthome.backend.dto.DeviceDTO;
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
}
