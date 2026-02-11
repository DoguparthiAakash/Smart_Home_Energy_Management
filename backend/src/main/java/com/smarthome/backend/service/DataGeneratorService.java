package com.smarthome.backend.service;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UsageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DataGeneratorService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UsageLogRepository usageLogRepository;

    // Run every 1 minute for demo purposes (60,000 ms)
    @Scheduled(fixedRate = 60000)
    public void generateUsageForActiveDevices() {
        LocalDateTime now = LocalDateTime.now();
        List<Device> activeDevices = deviceRepository.findAll().stream()
                .filter(Device::getStatus)
                .toList();

        if (activeDevices.isEmpty()) {
            return;
        }

        System.out.println("Generating usage for " + activeDevices.size() + " active devices at " + now);

        for (Device device : activeDevices) {
            // Calculate duration since last update or start time
            // For simplicity in this demo generator, we assume a 1-minute slice
            // In a real system, we'd track 'lastLogTime' to be precise.
            // Here we just log 1 minute of usage for every active device.
            
            double minutes = 1.0;
            double energyKwh = (device.getPowerRating() * minutes) / 60000.0;

            UsageLog log = new UsageLog();
            log.setDevice(device);
            log.setStartTime(now.minusMinutes(1)); // Approximation
            log.setEndTime(now);
            log.setTimestamp(now);
            log.setEnergyKwh(energyKwh);

            usageLogRepository.save(log);
            
            // Update lastStartTime to now to prevent double counting if toggle happens?
            // Actually, the toggle logic calculates from lastStartTime. 
            // If we are logging here, we should probably reset lastStartTime to NOW 
            // so that if the user turns it off, it only calculates the remainder.
            device.setLastStartTime(now);
            deviceRepository.save(device);
        }
    }
}
