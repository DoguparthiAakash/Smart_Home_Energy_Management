package com.smarthome.backend.controller;

import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.service.UsageLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private UsageLogService usageLogService;

    @GetMapping("/usage")
    public Map<String, Object> getUsageStats(@RequestParam(defaultValue = "daily") String period,
            Authentication authentication) {
        List<UsageLog> logs = usageLogService.getUsageForUser(authentication.getName(), period);
        Double totalCost = usageLogService.calculateTotalCost(logs);

        Map<String, Object> response = new HashMap<>();
        response.put("period", period);
        response.put("logs", logs);
        response.put("totalCost", totalCost);

        return response;
    }
}
