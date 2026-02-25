package com.smarthome.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot_password";
    }

    @GetMapping("/login-2fa")
    public String login2fa() {
        return "login-2fa";
    }

    @Autowired
    private com.smarthome.backend.repository.UserRepository userRepository;

    @GetMapping("/admin/ide")
    public String adminIde(Model model, Principal principal) {
        if (principal != null)
            model.addAttribute("userRole", "ROLE_ADMIN");
        return "ide";
    }

    @GetMapping({ "/admin" })
    public String admin(Model model, Principal principal) {
        if (principal != null)
            model.addAttribute("userRole", "ROLE_ADMIN");
        return "admin";
    }

    @GetMapping("/admin/control-panel")
    public String adminControlPanel(Model model, Principal principal) {
        if (principal != null)
            model.addAttribute("userRole", "ROLE_ADMIN");
        return "control-panel";
    }

    @GetMapping({ "/", "/home", "/dashboard" })
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("userRole", authentication.getAuthorities().toString());
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                model.addAttribute("maxWattage", user.getMaxWattage() != null ? user.getMaxWattage() : 5000.0);
            });
        }
        return "home";
    }

    @GetMapping("/usage")
    public String usage(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst().map(GrantedAuthority::getAuthority).orElse("ROLE_USER");
            model.addAttribute("userRole", role);
        }
        return "usage";
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        if (authentication != null) {
            String role = authentication.getAuthorities().stream()
                    .findFirst().map(GrantedAuthority::getAuthority).orElse("ROLE_USER");
            model.addAttribute("userRole", role);
        }
        return "settings";
    }
}
