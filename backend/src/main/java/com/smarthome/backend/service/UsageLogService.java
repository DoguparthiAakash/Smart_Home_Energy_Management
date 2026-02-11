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

    // TN Tariff Logic (Domestic) - Updated 2024
    // 0-100 units: Free (Subsidy)
    // 101-200 units: 2.25
    // 201-400 units: 4.80 (Consolidated for demo simplicity as per search results: 0-400 @ 4.80 but first 100 free)
    // Let's implement the specific logic:
    // Bi-monthly logic adapted for monthly/daily estimation.
    // For calculation simplicity, we will apply the rate based on total units accumulated.
    
    public Double calculateCost(Double currentUsageKwh) {
        if (currentUsageKwh == null) return 0.0;
        
        // This function calculates cost for a specific usage amount assuming it is the *incremental* usage
        // pushing through slabs? No, for accurate billing we need TOTAL usage for the billing period.
        // However, the current architecture sums up daily costs. 
        // To support "live" cost display without refactoring the entire billing engine:
        // We will assume a simplified PRO-RATA daily slab or just a tiered rate based on current volume.
        
        // BETTER APPROACH FOR DEMO:
        // Use a tiered rate function.
        // <= 100 units: 0
        // > 100 && <= 400: 4.80
        // > 400 && <= 500: 6.45
        // > 500: 8.55
        
        // Note: In real billing, the first 100 are free, next 100 are charged X, etc.
        // But since we are calculating cost per *UsageLog* (or small batches), we can't easily know 
        // "where" in the slab this specific unit falls without contextualizing total month usage.
        
        // COMPROMISE:
        // for `calculateCost(totalUsage)`, we apply the full slab logic.
        // for `calculateCost(small_increment)`, we might underestimate if we don't know the base.
        
        // Let's treat the input `currentUsageKwh` as the TOTAL accumulated usage for the period 
        // (which is how `calculateTotalCost` and the Controller use it for history).
        
        double totalUnits = currentUsageKwh;
        double totalCost = 0.0;
        
        if (totalUnits <= 100) {
            return 0.0; 
        }
        
        // First 100 free
        totalUnits -= 100;
        
        // Next 300 (101-400) @ 4.80
        if (totalUnits <= 300) {
            totalCost += totalUnits * 4.80;
            return totalCost;
        } else {
            totalCost += 300 * 4.80;
            totalUnits -= 300;
        }
        
        // Next 100 (401-500) @ 6.45
        if (totalUnits <= 100) {
            totalCost += totalUnits * 6.45;
            return totalCost;
        } else {
            totalCost += 100 * 6.45;
            totalUnits -= 100;
        }
        
        // Above 500 @ 8.55
        totalCost += totalUnits * 8.55;
        
        return totalCost;
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
}
