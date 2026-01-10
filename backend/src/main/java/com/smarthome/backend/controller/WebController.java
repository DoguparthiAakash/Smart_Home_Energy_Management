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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({ "/", "/home", "/dashboard", "/admin" })
    public String home(Model model, org.springframework.security.core.Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(r -> r.getAuthority())
                    .orElse("ROLE_USER");
            model.addAttribute("userRole", role);
        }
        return "home";
    }

    @GetMapping("/usage")
    public String usage() {
        return "usage";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }
}
