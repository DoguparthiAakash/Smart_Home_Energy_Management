package com.smarthome.backend.controller;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {
        String role = authentication.getAuthorities().toString();
        model.addAttribute("userRole", role);

        // Technicians can see all devices to diagnose them
        List<Device> allDevices = deviceRepository.findAll();
        model.addAttribute("devices", allDevices);

        return "technician";
    }

    // API for AJAX calls to get voltage
    @GetMapping("/api/voltage/{deviceId}")
    @ResponseBody
    public ResponseEntity<Double> getDeviceVoltage(@PathVariable Long deviceId) {
        // Simulate voltage reading between 220V and 240V
        double voltage = 220 + new Random().nextDouble() * 20;
        // Format to 1 decimal place? No, frontend handles display usually, but let's
        // keep it raw double
        return ResponseEntity.ok(voltage);
    }
}
