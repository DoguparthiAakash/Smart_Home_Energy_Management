package com.smarthome.backend.controller;

import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/devices/energy")
public class DeviceExportController {

    @Autowired
    private UsageLogRepository usageLogRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/export/custom")
    public ResponseEntity<byte[]> exportCustom(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        Long userId = (user.getRole() == User.Role.ADMIN) ? null : user.getId();

        List<UsageLog> logs = usageLogRepository.findByFilters(userId, deviceId, category, start, end);

        StringBuilder csv = new StringBuilder("ID,Device Name,Device Type,Location,Start Time,End Time,Energy (kWh)\n");
        for (UsageLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append("\"").append(log.getDevice().getName()).append("\",");
            csv.append(log.getDevice().getType()).append(",");
            csv.append(log.getDevice().getLocation() != null ? log.getDevice().getLocation() : "N/A").append(",");
            csv.append(log.getStartTime() != null ? log.getStartTime() : "N/A").append(",");
            csv.append(log.getEndTime() != null ? log.getEndTime() : "N/A").append(",");
            csv.append(String.format("%.4f", log.getEnergyKwh())).append("\n");
        }

        byte[] csvBytes = csv.toString().getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usage_report.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvBytes);
    }
}
