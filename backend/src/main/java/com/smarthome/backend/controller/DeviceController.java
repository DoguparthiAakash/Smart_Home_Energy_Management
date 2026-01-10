package com.smarthome.backend.controller;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public DeviceDTO toggleDevice(@PathVariable Long id) {
        return deviceService.toggleDevice(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok().build();
    }

    // New Endpoints for Wattage Limit
    @GetMapping("/limit")
    public ResponseEntity<Double> getWattageLimit() {
        return ResponseEntity.ok(deviceService.getMaxWattage());
    }

    @PostMapping("/limit")
    public ResponseEntity<?> setWattageLimit(@RequestBody Map<String, Double> payload) {
        if (payload.containsKey("limit")) {
            deviceService.setMaxWattage(payload.get("limit"));
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // --- Advanced Provisioning ---

    @PostMapping("/pair")
    public ResponseEntity<?> pairDevice(@RequestBody Map<String, String> payload, Authentication authentication) {
        try {
            String code = payload.get("code");
            return ResponseEntity.ok(deviceService.pairDevice(authentication.getName(), code));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/discover")
    public ResponseEntity<List<DeviceDTO>> discoverDevices() {
        return ResponseEntity.ok(deviceService.getDiscoveredDevices());
    }
}
