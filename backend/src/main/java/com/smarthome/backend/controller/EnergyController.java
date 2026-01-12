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

    @org.springframework.beans.factory.annotation.Autowired
    private com.smarthome.backend.repository.DeviceRepository deviceRepository;

    @GetMapping("/summary")
    public Map<String, Object> getEnergySummary() {
        // Fetch all devices from DB
        java.util.List<com.smarthome.backend.model.Device> devices = deviceRepository.findAll();

        // Calculate real-time active load
        double totalActiveWatts = devices.stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0.0)
                .sum();

        // Algorithm: Assume devices run for approx 8 hours/day on average effective
        // load
        // Usage (kWh) = (Watts / 1000) * Hours
        double dailyUsageKwh = (totalActiveWatts / 1000.0) * 8.0;

        // Add a small base load (e.g., WiFi, Standby) of 2 kWh even if everything is
        // off
        dailyUsageKwh += 2.0;

        double ratePerKwh = 8.0; // ₹8.00 per kWh
        double estimatedCost = dailyUsageKwh * ratePerKwh;

        // Calculate Total Potential Capacity for Progress Bar scale
        double totalPotentialWatts = devices.stream()
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0.0)
                .sum();
        if (totalPotentialWatts < 1.0)
            totalPotentialWatts = 5000.0; // Default fallback

        Map<String, Object> response = new HashMap<>();
        response.put("usage", Math.round(dailyUsageKwh * 100.0) / 100.0);
        response.put("unit", "kWh");
        response.put("cost", Math.round(estimatedCost * 100.0) / 100.0);
        response.put("currency", "₹");
        response.put("currentWatts", totalActiveWatts);
        response.put("capacityWatts", totalPotentialWatts);
        return response;
    }

    @GetMapping("/history")
    public Map<String, Object> getEnergyHistory() {
        // Fetch total potential load to scale the chart
        java.util.List<com.smarthome.backend.model.Device> devices = deviceRepository.findAll();
        double totalPotentialWatts = devices.stream()
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0.0)
                .sum();

        // Convert to Max Possible Daily kWh (running 24h)
        double maxDailyKwh = (totalPotentialWatts / 1000.0) * 24.0;
        if (maxDailyKwh < 5.0)
            maxDailyKwh = 10.0; // Fallback for empty homes

        // Fixed pattern to ensure stability (Mon-Sun profile)
        // Usage pattern factors (0.0 to 1.0 of max capacity)
        double[] weeklyPattern = { 0.4, 0.45, 0.5, 0.48, 0.6, 0.75, 0.55 };

        double[] data = new double[7];
        for (int i = 0; i < 7; i++) {
            data[i] = Math.round(maxDailyKwh * weeklyPattern[i] * 10.0) / 10.0;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" });
        response.put("data", data);
        return response;
    }
}
