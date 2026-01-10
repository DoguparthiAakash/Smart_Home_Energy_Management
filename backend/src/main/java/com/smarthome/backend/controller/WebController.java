package com.smarthome.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private com.smarthome.backend.repository.UserRepository userRepository;

    @GetMapping({ "/", "/home", "/dashboard", "/admin" })
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().toString();
            model.addAttribute("userRole", role);

            // Inject Max Wattage for Frontend
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                model.addAttribute("maxWattage", user.getMaxWattage() != null ? user.getMaxWattage() : 5000.0);
            });
        }
        return "home";
    }

    @GetMapping("/usage")
    public String usage() {
        return "usage";
    }
}
