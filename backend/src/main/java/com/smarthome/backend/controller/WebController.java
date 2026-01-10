package com.smarthome.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({ "/", "/home", "/dashboard", "/admin" })
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().toString();
            model.addAttribute("userRole", role);
        }
        return "home";
    }

    @GetMapping("/usage")
    public String usage() {
        return "usage";
    }
}
