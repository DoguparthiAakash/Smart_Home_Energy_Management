package com.smarthome.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/energy")
public class EnergyController {

    @GetMapping("/summary")
    public Map<String, Object> getEnergySummary() {
        // Simulate real-time calculation
        double currentUsage = 45.3 + (new Random().nextDouble() * 5); // 45-50 kWh
        double ratePerKwh = 0.15; // $0.15
        double estimatedCost = currentUsage * ratePerKwh;

        Map<String, Object> response = new HashMap<>();
        response.put("usage", Math.round(currentUsage * 100.0) / 100.0);
        response.put("unit", "kWh");
        response.put("cost", Math.round(estimatedCost * 100.0) / 100.0);
        response.put("currency", "$");
        return response;
    }

    @GetMapping("/history")
    public Map<String, Object> getEnergyHistory() {
        // Simulate weekly data
        int[] data = new int[7];
        Random rand = new Random();
        for (int i = 0; i < 7; i++) {
            data[i] = rand.nextInt(20) + 5; // 5-25 kWh per day
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" });
        response.put("data", data);
        return response;
    }
}
