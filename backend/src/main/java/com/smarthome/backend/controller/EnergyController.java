package com.smarthome.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
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

        java.util.List<com.smarthome.backend.model.Device> devices = deviceRepository.findByUserId(user.getId());

        double totalActiveWatts = devices.stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .mapToDouble(d -> d.getPowerRating() != null ? d.getPowerRating() : 0.0)
                .sum();

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        Double dailyUsageKwh = usageLogService.getDailyUsage(userEmail, startOfDay, now);
        if (dailyUsageKwh == null)
            dailyUsageKwh = 0.0;

        double estimatedCost = usageLogService.calculateCost(dailyUsageKwh);
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
        double[] costData = new double[7];

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            labels[i] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Double dailySum = usageLogService.getDailyUsage(userEmail, start, end);
            double val = dailySum != null ? Math.round(dailySum * 100.0) / 100.0 : 0.0;
            data[i] = val;
            costData[i] = Math.round(usageLogService.calculateCost(val) * 100.0) / 100.0;
        }

        // Weekly totals & averages
        double totalUsage = 0;
        for (double d : data)
            totalUsage += d;

        double totalCost = usageLogService.calculateCost(totalUsage);
        double avgUsage = totalUsage / 7.0;
        double avgCost = totalCost / 7.0;

        // Rate info for frontend display
        Map<String, Object> rateInfo = new HashMap<>();
        rateInfo.put("slab1", "0–50 kWh @ ₹5.00/unit");
        rateInfo.put("slab2", "51–200 kWh @ ₹7.00/unit");
        rateInfo.put("slab3", "201–500 kWh @ ₹9.00/unit");
        rateInfo.put("slab4", ">500 kWh @ ₹12.00/unit");

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        response.put("costData", costData);
        response.put("totalUsage", Math.round(totalUsage * 100.0) / 100.0);
        response.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        response.put("avgUsage", Math.round(avgUsage * 100.0) / 100.0);
        response.put("avgCost", Math.round(avgCost * 100.0) / 100.0);
        response.put("rateInfo", rateInfo);
        return response;
    }

    @GetMapping("/history/custom")
    public Map<String, Object> getCustomEnergyHistory(
            @org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @org.springframework.web.bind.annotation.RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            org.springframework.security.core.Authentication authentication) {

        String userEmail = authentication.getName();

        // Calculate days between
        long days = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (days <= 0)
            days = 1;
        if (days > 31)
            days = 31; // Limit to 31 days for performance

        String[] labels = new String[(int) days];
        double[] data = new double[(int) days];
        double[] costData = new double[(int) days];

        LocalDate startDate = start.toLocalDate();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            labels[i] = date.getMonthValue() + "/" + date.getDayOfMonth();

            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            Double dailySum = usageLogService.getDailyUsage(userEmail, dayStart, dayEnd);
            double val = dailySum != null ? Math.round(dailySum * 100.0) / 100.0 : 0.0;
            data[i] = val;
            costData[i] = Math.round(usageLogService.calculateCost(val) * 100.0) / 100.0;
        }

        double totalUsage = 0;
        for (double d : data)
            totalUsage += d;

        double totalCost = usageLogService.calculateCost(totalUsage);
        double avgUsage = totalUsage / (double) days;
        double avgCost = totalCost / (double) days;

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("data", data);
        response.put("costData", costData);
        response.put("totalUsage", Math.round(totalUsage * 100.0) / 100.0);
        response.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        response.put("avgUsage", Math.round(avgUsage * 100.0) / 100.0);
        response.put("avgCost", Math.round(avgCost * 100.0) / 100.0);
        return response;
    }

    @GetMapping("/device/{id}/summary")
    public Map<String, Object> getDeviceUsageSummary(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime startOfWeek = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        double todayKwh = usageLogService.getDeviceDailyUsage(id, startOfDay, now);
        double weekKwh = usageLogService.getDeviceDailyUsage(id, startOfWeek, now);

        Map<String, Object> res = new HashMap<>();
        res.put("todayKwh", Math.round(todayKwh * 1000.0) / 1000.0);
        res.put("weekKwh", Math.round(weekKwh * 1000.0) / 1000.0);
        res.put("todayCost", Math.round(usageLogService.calculateCost(todayKwh) * 100.0) / 100.0);
        res.put("weekCost", Math.round(usageLogService.calculateCost(weekKwh) * 100.0) / 100.0);
        return res;
    }

    @GetMapping("/device/{id}/history")
    public List<Map<String, Object>> getDeviceUsageHistory(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        return usageLogService.getLogsForDevice(id, start, end).stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(20)
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", log.getTimestamp());
                    map.put("energyKwh", Math.round(log.getEnergyKwh() * 1000.0) / 1000.0);
                    map.put("cost", Math.round(usageLogService.calculateCost(log.getEnergyKwh()) * 100.0) / 100.0);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/device-usage")
    public List<Map<String, Object>> getDeviceEnergyUsage(
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @org.springframework.web.bind.annotation.RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            org.springframework.security.core.Authentication authentication) {
        String userEmail = authentication.getName();

        if (start == null) {
            start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }

        return usageLogService.getUsagePerDevice(userEmail, start, end).stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", obj[0]);
                    map.put("usage", obj[1] != null ? Math.round((Double) obj[1] * 1000.0) / 1000.0 : 0.0);
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/hourly")
    public java.util.Map<Integer, Double> getHourlyUsage(
            org.springframework.security.core.Authentication authentication) {
        String userEmail = authentication.getName();
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now();
        return usageLogService.getHourlyUsageForUser(userEmail, start, end);
    }
}
