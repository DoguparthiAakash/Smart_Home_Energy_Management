package com.smarthome.backend.service;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.DeviceSchedule;
import com.smarthome.backend.repository.DeviceScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DeviceScheduleService {

    private static final Logger log = LoggerFactory.getLogger(DeviceScheduleService.class);

    @Autowired
    private DeviceScheduleRepository scheduleRepository;

    @Autowired
    private DeviceService deviceService;

    /**
     * Runs every minute to check if any device should be turned ON or OFF
     * based on its schedule.
     */
    @Scheduled(cron = "0 * * * * *")
    public void processSchedules() {
        log.debug("Processing device schedules...");
        List<DeviceSchedule> activeSchedules = scheduleRepository.findByActiveTrue();
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

        for (DeviceSchedule schedule : activeSchedules) {
            Device device = schedule.getDevice();

            // Check ON time
            if (schedule.getScheduledOnTime() != null) {
                LocalTime onTime = schedule.getScheduledOnTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
                if (onTime.equals(now) && !device.getStatus()) {
                    log.info("[Scheduler] Turning ON device: {} ({}) based on schedule", device.getName(),
                            device.getId());
                    try {
                        deviceService.toggleDevice(device.getId());
                    } catch (Exception e) {
                        log.error("[Scheduler] Failed to turn ON device {}: {}", device.getId(), e.getMessage());
                    }
                }
            }

            // Check OFF time
            if (schedule.getScheduledOffTime() != null) {
                LocalTime offTime = schedule.getScheduledOffTime().toLocalTime().truncatedTo(ChronoUnit.MINUTES);
                if (offTime.equals(now) && device.getStatus()) {
                    log.info("[Scheduler] Turning OFF device: {} ({}) based on schedule", device.getName(),
                            device.getId());
                    try {
                        deviceService.toggleDevice(device.getId());
                    } catch (Exception e) {
                        log.error("[Scheduler] Failed to turn OFF device {}: {}", device.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}
