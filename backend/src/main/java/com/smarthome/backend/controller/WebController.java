package com.smarthome.backend.controller;

import com.smarthome.backend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import com.smarthome.backend.dto.DeviceDTO; // Added import

@Controller
public class WebController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping("/")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/schedule")
    public String schedule() {
        return "schedule";
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) String token, Model model) {
        // Mock user session for demo
        List<DeviceDTO> devices = deviceService.getDevicesForUser("user@smarthome.com");
        model.addAttribute("devices", devices);
        return "dashboard";
    }
}
