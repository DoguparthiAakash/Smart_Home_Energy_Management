package com.smarthome.backend.service;

import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageLogService {

    @Autowired
    private UsageLogRepository usageLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Default rate: ₹7.00 per kWh (flat rate, no state tariff)
    // Tiered pricing:
    // 0–50 kWh → ₹5.00/kWh (low usage discount)
    // 51–200 kWh → ₹7.00/kWh (standard rate)
    // 201–500 kWh → ₹9.00/kWh (medium usage)
    // >500 kWh → ₹12.00/kWh (high usage)
    private static final double RATE_LOW = 5.00;
    private static final double RATE_STD = 7.00;
    private static final double RATE_MED = 9.00;
    private static final double RATE_HIGH = 12.00;

    public List<UsageLog> getUsageForUser(String userEmail, String period) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (period.toLowerCase()) {
            case "weekly":
                start = end.minusWeeks(1);
                break;
            case "monthly":
                start = end.minusMonths(1);
                break;
            case "daily":
            default:
                start = end.minusDays(1);
                break;
        }

        return usageLogRepository.findByUserIdAndTimestampBetween(user.getId(), start, end);
    }

    /**
     * Tiered cost calculation (no state-specific tariff).
     * Treats totalUsageKwh as the accumulated total for the billing period.
     *
     * Slabs:
     * 0–50 kWh → ₹5.00/unit
     * 51–200 kWh → ₹7.00/unit
     * 201–500 kWh → ₹9.00/unit
     * >500 kWh → ₹12.00/unit
     */
    public Double calculateCost(Double usageKwh) {
        if (usageKwh == null || usageKwh <= 0)
            return 0.0;

        double units = usageKwh;
        double cost = 0.0;

        // Slab 1: 0–50 kWh @ ₹5.00
        double slab1 = Math.min(units, 50);
        cost += slab1 * RATE_LOW;
        units -= slab1;
        if (units <= 0)
            return Math.round(cost * 100.0) / 100.0;

        // Slab 2: 51–200 kWh @ ₹7.00
        double slab2 = Math.min(units, 150);
        cost += slab2 * RATE_STD;
        units -= slab2;
        if (units <= 0)
            return Math.round(cost * 100.0) / 100.0;

        // Slab 3: 201–500 kWh @ ₹9.00
        double slab3 = Math.min(units, 300);
        cost += slab3 * RATE_MED;
        units -= slab3;
        if (units <= 0)
            return Math.round(cost * 100.0) / 100.0;

        // Slab 4: >500 kWh @ ₹12.00
        cost += units * RATE_HIGH;

        return Math.round(cost * 100.0) / 100.0;
    }

    public Double calculateTotalCost(List<UsageLog> logs) {
        double totalKwh = logs.stream()
                .mapToDouble(UsageLog::getEnergyKwh)
                .sum();
        return calculateCost(totalKwh);
    }

    public Double getDailyUsage(String userEmail, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return usageLogRepository.findByUserIdAndTimestampBetween(user.getId(), start, end)
                .stream()
                .mapToDouble(UsageLog::getEnergyKwh)
                .sum();
    }

    public Double getDeviceDailyUsage(Long deviceId, LocalDateTime start, LocalDateTime end) {
        return usageLogRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end)
                .stream()
                .mapToDouble(UsageLog::getEnergyKwh)
                .sum();
    }

    public List<UsageLog> getLogsForDevice(Long deviceId, LocalDateTime start, LocalDateTime end) {
        return usageLogRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end);
    }

    public List<Object[]> getUsagePerDevice(String userEmail, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return usageLogRepository.findUsagePerDevice(user.getId(), start, end);
    }

    public java.util.Map<Integer, Double> getHourlyUsageForUser(String userEmail, LocalDateTime start,
            LocalDateTime end) {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        List<UsageLog> logs = usageLogRepository.findByUserIdAndTimestampBetween(user.getId(), start, end);

        java.util.Map<Integer, Double> hourlyMap = new java.util.TreeMap<>();
        for (int i = 0; i < 24; i++)
            hourlyMap.put(i, 0.0);

        for (UsageLog log : logs) {
            int hour = log.getTimestamp().getHour();
            hourlyMap.put(hour, hourlyMap.get(hour) + log.getEnergyKwh());
        }
        return hourlyMap;
    }
}
