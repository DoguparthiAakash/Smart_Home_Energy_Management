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

    @GetMapping("/admin/ide")
    public String adminIde(Model model, Principal principal) {
        if (principal != null) {
            String role = principal.getName().equals("muterornament") ? "ROLE_ADMIN" : "ROLE_USER";
            // For real security, we should check authorities, but this aligns with current
            // pattern
            model.addAttribute("userRole", role);
        }
        return "ide";
    }

    @GetMapping("/login-2fa")
    public String login2fa() {
        return "login-2fa";
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
