package com.smarthome.backend.service;

import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Double calculateTotalCost(List<UsageLog> logs) {
        // Simple logic: 0.15$ per kWh
        return logs.stream().mapToDouble(UsageLog::getEnergyKwh).sum() * 0.15;
    }
}
