package com.smarthome.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // Legacy Auth Controller replaced by Spring Security Session Login
    // No endpoints here to prevent conflict.
}
