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
    public String adminIde(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }
        return "ide";
    }

    @GetMapping({ "/admin" })
    public String admin(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }
        return "admin";
    }

    @GetMapping("/admin/control-panel")
    public String adminControlPanel(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }
        return "control-panel";
    }

    @GetMapping({ "/", "/home", "/dashboard" })
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin";
            }
            model.addAttribute("isAdmin", false);
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
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }
        return "usage";
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("userRole", authentication.getAuthorities().toString());
        }
        return "settings";
    }
}
