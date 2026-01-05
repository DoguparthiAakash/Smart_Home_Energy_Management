package com.smarthome.backend.controller;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
        // In a real app, validation of token should happen here.
        // For simple Thymeleaf rendering, we will fetch devices directly.
        // We assume User ID 1 for demo purposes if not strictly authenticated via
        // Cookie here.
        // Ideally, we'd use Spring Security session for web.
        // We'll mock fetching devices for a specific user (e.g., user 1).

        List<Device> devices = deviceService.getDevicesByUserId(1L);
        model.addAttribute("devices", devices);
        return "dashboard";
    }
}
