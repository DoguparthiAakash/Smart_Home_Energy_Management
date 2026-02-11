package com.smarthome.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smarthome.backend.repository.UsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/energy")
public class EnergyController {

    @Autowired
    private com.smarthome.backend.repository.DeviceRepository deviceRepository;

    @Autowired
    private com.smarthome.backend.service.UsageLogService usageLogService;

    @Autowired
    private com.smarthome.backend.repository.UserRepository userRepository;

    @GetMapping("/summary")
    public Map<String, Object> getEnergySummary(org.springframework.security.core.Authentication authentication) {
        String userEmail = authentication.getName();
        com.smarthome.backend.model.User user = userRepository.findByEmail(userEmail).orElseThrow();

        // Fetch user's devices
        java.util.List<com.smarthome.backend.model.Device> devices = deviceRepository.findByUserId(user.getId());

        // Calculate real-time active load
        double totalActiveWatts = devices.stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0.0)
                .sum();

        // Calculate usage for today
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        
        Double dailyUsageKwh = usageLogService.getDailyUsage(userEmail, startOfDay, now);
        if (dailyUsageKwh == null) {
            dailyUsageKwh = 0.0;
        }

        double estimatedCost = usageLogService.calculateCost(dailyUsageKwh);

        // Calculate Total Potential Capacity for Progress Bar scale
        double totalPotentialWatts = user.getMaxWattage() != null ? user.getMaxWattage() : 5000.0;

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
    public Map<String, Object> getEnergyHistory(org.springframework.security.core.Authentication authentication) {
        String userEmail = authentication.getName();

        String[] labels = new String[7];
        double[] data = new double[7];

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            labels[i] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Double dailySum = usageLogService.getDailyUsage(userEmail, start, end);
            double val = dailySum != null ? Math.round(dailySum * 100.0) / 100.0 : 0.0;
            data[i] = val;
        }

        // Calculate Totals & Averages for the week
        double totalUsage = 0;
        for (double d : data)
            totalUsage += d;

        double totalCost = usageLogService.calculateCost(totalUsage);
        double avgUsage = totalUsage / 7.0;
        double avgCost = totalCost / 7.0;

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        response.put("totalUsage", Math.round(totalUsage * 100.0) / 100.0);
        response.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        response.put("avgUsage", Math.round(avgUsage * 100.0) / 100.0);
        response.put("avgCost", Math.round(avgCost * 100.0) / 100.0);
        return response;
    }
}
