package com.smarthome.backend.service;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceEventRepository;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.DeviceScheduleRepository;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.model.SystemEvent;
import com.smarthome.backend.repository.SystemEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import com.smarthome.backend.dto.DeviceScheduleDTO;
import com.smarthome.backend.model.DeviceSchedule;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageLogRepository usageLogRepository;

    @Autowired
    private DeviceEventRepository eventRepository;

    @Autowired
    private DeviceScheduleRepository scheduleRepository;

    @Autowired
    private SystemEventRepository systemEventRepository;

    public List<DeviceDTO> getDevicesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.GUEST) {
            return deviceRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        return deviceRepository.findByUserId(user.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DeviceDTO addDevice(String userEmail, DeviceDTO deviceDTO) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        Device device = new Device();
        device.setUser(user);
        device.setName(deviceDTO.getName());
        device.setType(deviceDTO.getType());
        device.setPowerRating(deviceDTO.getPowerRating());
        device.setStatus(false);
        device.setLocation(deviceDTO.getLocation());
        if (deviceDTO.getPowerLimit() != null) {
            device.setPowerLimit(deviceDTO.getPowerLimit());
        }
        try {
            device.setPriority(Device.Priority.valueOf(deviceDTO.getPriority()));
        } catch (Exception e) {
            device.setPriority(Device.Priority.LOW);
        }

        Device saved = deviceRepository.save(device);
        logSystemEvent("DEVICE_ADDED",
                "New device '" + saved.getName() + "' (" + saved.getType() + ") added by " + userEmail,
                SystemEvent.Severity.INFO);
        return convertToDTO(saved);
    }

    public DeviceDTO updateDevice(Long deviceId, DeviceDTO deviceDTO) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        if (deviceDTO.getName() != null)
            device.setName(deviceDTO.getName());
        if (deviceDTO.getLocation() != null)
            device.setLocation(deviceDTO.getLocation());
        if (deviceDTO.getPowerLimit() != null)
            device.setPowerLimit(deviceDTO.getPowerLimit());
        if (deviceDTO.getPowerRating() != null)
            device.setPowerRating(deviceDTO.getPowerRating());
        if (deviceDTO.getPriority() != null) {
            try {
                device.setPriority(Device.Priority.valueOf(deviceDTO.getPriority()));
            } catch (Exception ignored) {
            }
        }
        if (deviceDTO.getCustomIcon() != null) {
            device.setCustomIcon(deviceDTO.getCustomIcon());
        }
        Device updated = deviceRepository.save(device);
        logSystemEvent("DEVICE_UPDATED", "Device '" + updated.getName() + "' configuration updated",
                SystemEvent.Severity.INFO);
        return convertToDTO(updated);
    }

    public DeviceDTO updateDeviceIcon(Long deviceId, String base64Icon) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setCustomIcon(base64Icon);
        return convertToDTO(deviceRepository.save(device));
    }

    public DeviceDTO toggleDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        LocalDateTime now = LocalDateTime.now();

        if (device.getStatus()) {
            // Turning OFF - Log Usage
            if (device.getLastStartTime() != null) {
                long minutes = Duration.between(device.getLastStartTime(), now).toMinutes();
                if (minutes > 0) {
                    double energyKwh = (device.getPowerRating() * minutes) / 60000.0;
                    UsageLog log = new UsageLog();
                    log.setDevice(device);
                    log.setStartTime(device.getLastStartTime());
                    log.setEndTime(now);
                    log.setTimestamp(now);
                    log.setEnergyKwh(energyKwh);
                    usageLogRepository.save(log);
                }
            }
            device.setStatus(false);
            device.setLastStartTime(null);
        } else {
            // Turning ON - Check Load & Set Start Time
            User owner = device.getUser();
            double maxWattage = owner.getMaxWattage() != null ? owner.getMaxWattage() : 5000.0;

            // Also respect device-level power limit
            double effectiveMax = (device.getPowerLimit() != null) ? Math.min(maxWattage, device.getPowerLimit())
                    : maxWattage;

            double currentLoad = calculateCurrentLoad(owner.getId());
            double potentialLoad = currentLoad + device.getPowerRating();

            if (potentialLoad > effectiveMax) {
                double required = potentialLoad - effectiveMax;
                boolean success = shedLoad(owner.getId(), required);
                if (!success) {
                    throw new RuntimeException(
                            "Cannot turn on device. Max wattage exceeded and no lower priority devices to shed.");
                }
            }

            device.setStatus(true);
            device.setLastStartTime(now);
        }

        return convertToDTO(deviceRepository.save(device));
    }

    private double calculateCurrentLoad(Long userId) {
        return deviceRepository.findByUserId(userId).stream()
                .filter(Device::getStatus)
                .mapToDouble(Device::getPowerRating)
                .sum();
    }

    private boolean shedLoad(Long userId, double requiredWattage) {
        List<Device> activeDevices = deviceRepository.findByUserId(userId).stream()
                .filter(Device::getStatus)
                .collect(Collectors.toList());

        List<Device> shedCandidates = activeDevices.stream()
                .filter(d -> d.getPriority() != Device.Priority.HIGH)
                .sorted((d1, d2) -> d2.getPriority().compareTo(d1.getPriority()))
                .collect(Collectors.toList());

        double shedAmount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Device d : shedCandidates) {
            if (d.getLastStartTime() != null) {
                long minutes = Duration.between(d.getLastStartTime(), now).toMinutes();
                if (minutes > 0) {
                    double energyKwh = (d.getPowerRating() * minutes) / 60000.0;
                    UsageLog log = new UsageLog();
                    log.setDevice(d);
                    log.setStartTime(d.getLastStartTime());
                    log.setEndTime(now);
                    log.setTimestamp(now);
                    log.setEnergyKwh(energyKwh);
                    usageLogRepository.save(log);
                }
            }
            d.setStatus(false);
            d.setLastStartTime(null);
            deviceRepository.save(d);

            shedAmount += d.getPowerRating();
            logSystemEvent("ENERGY_MANAGEMENT",
                    "Smart throttle: " + d.getName() + " was deactivated to maintain grid stability.",
                    SystemEvent.Severity.INFO);
            if (shedAmount >= requiredWattage) {
                return true;
            }
        }

        return shedAmount >= requiredWattage;
    }

    @Transactional
    public void deleteDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        // Manual cleanup of dependent entities
        usageLogRepository.deleteByDeviceId(deviceId);
        eventRepository.deleteByDeviceId(deviceId);
        scheduleRepository.deleteByDeviceId(deviceId);
        deviceRepository.delete(device);
        logSystemEvent("DEVICE_DELETED", "Device '" + device.getName() + "' removed from system",
                SystemEvent.Severity.WARNING);
    }

    private void logSystemEvent(String type, String message, SystemEvent.Severity severity) {
        SystemEvent event = SystemEvent.builder()
                .type(type)
                .message(message)
                .severity(severity)
                .timestamp(LocalDateTime.now())
                .build();
        systemEventRepository.save(event);
    }

    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setType(device.getType());
        dto.setPowerRating(device.getPowerRating());
        dto.setStatus(device.getStatus());
        dto.setPriority(device.getPriority().name());
        dto.setLocation(device.getLocation());
        dto.setPowerLimit(device.getPowerLimit());
        dto.setFirmwareVersion(device.getFirmwareVersion() != null ? device.getFirmwareVersion() : "1.0.0");
        dto.setHealthStatus(device.getHealthStatus() != null ? device.getHealthStatus() : "EXCELLENT");
        dto.setCustomIcon(device.getCustomIcon());
        return dto;
    }

    public DeviceScheduleDTO getSchedule(Long deviceId) {
        List<DeviceSchedule> schedules = scheduleRepository.findByDeviceId(deviceId);
        if (schedules.isEmpty()) {
            return new DeviceScheduleDTO("", false, "", false, null);
        }
        DeviceSchedule s = schedules.get(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String onTime = s.getScheduledOnTime() != null ? s.getScheduledOnTime().format(formatter) : "";
        String offTime = s.getScheduledOffTime() != null ? s.getScheduledOffTime().format(formatter) : "";
        return new DeviceScheduleDTO(onTime, s.getActive(), offTime, s.getActive(), s.getTimezone()); // Simplification:
                                                                                                      // one active flag
        // for both
    }

    @Transactional
    public void saveSchedule(Long deviceId, DeviceScheduleDTO dto) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        scheduleRepository.deleteByDeviceId(deviceId);

        DeviceSchedule s = new DeviceSchedule();
        s.setDevice(device);
        s.setActive(dto.isOnEnabled() || dto.isOffEnabled());
        s.setTimezone(dto.getTimezone());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (dto.getOnTime() != null && !dto.getOnTime().isEmpty()) {
            LocalTime time = LocalTime.parse(dto.getOnTime(), formatter);
            s.setScheduledOnTime(LocalDateTime.of(LocalDate.now(), time));
        }
        if (dto.getOffTime() != null && !dto.getOffTime().isEmpty()) {
            LocalTime time = LocalTime.parse(dto.getOffTime(), formatter);
            s.setScheduledOffTime(LocalDateTime.of(LocalDate.now(), time));
        }

        scheduleRepository.save(s);
    }
}
