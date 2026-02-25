package com.smarthome.backend.service;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UsageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * DeviceSimulatorService – runs every 30 seconds and logs energy consumption
 * for each device that is currently ON.
 *
 * Energy per 30-second interval = (powerRating_W * 30s) / 3,600,000 kWh
 * A ±15% random jitter is applied to simulate real-world sensor variance.
 */
@Service
public class DeviceSimulatorService {

    private static final Logger log = LoggerFactory.getLogger(DeviceSimulatorService.class);

    // Interval in seconds (must match fixedRate below)
    private static final int INTERVAL_SECONDS = 30;

    // Jitter factor: ±JITTER of rated power
    private static final double JITTER = 0.15;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UsageLogRepository usageLogRepository;

    private final Random random = new Random();

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("[Simulator] {} at {}", enabled ? "ENABLED" : "DISABLED", LocalDateTime.now());
    }

    @Scheduled(fixedRate = 30_000) // every 30 seconds
    public void simulateEnergyUsage() {
        if (!enabled)
            return;
        List<Device> onDevices = deviceRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .toList();

        if (onDevices.isEmpty())
            return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime intervalStart = now.minusSeconds(INTERVAL_SECONDS);

        for (Device device : onDevices) {
            double rated = device.getPowerRating() != null ? device.getPowerRating() : 0;
            if (rated <= 0)
                continue;

            // Apply ±JITTER variance
            double jitter = 1.0 + (random.nextDouble() * 2 * JITTER) - JITTER;
            double actualWatts = rated * jitter;

            // Energy in kWh for this 30-second interval
            double energyKwh = (actualWatts * INTERVAL_SECONDS) / 3_600_000.0;

            UsageLog entry = new UsageLog();
            entry.setDevice(device);
            entry.setStartTime(intervalStart);
            entry.setEndTime(now);
            entry.setTimestamp(now);
            entry.setEnergyKwh(energyKwh);
            usageLogRepository.save(entry);
        }

        log.debug("[Simulator] Logged {}s energy for {} device(s) at {}",
                INTERVAL_SECONDS, onDevices.size(), now);
    }
}
