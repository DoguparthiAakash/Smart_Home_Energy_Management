package com.smarthome.backend.service;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsageLogRepository usageLogRepository;

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
        try {
            device.setPriority(Device.Priority.valueOf(deviceDTO.getPriority()));
        } catch (Exception e) {
            device.setPriority(Device.Priority.LOW);
        }

        Device saved = deviceRepository.save(device);
        return convertToDTO(saved);
    }

    public DeviceDTO toggleDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        LocalDateTime now = LocalDateTime.now();

        if (device.getStatus()) {
            // Turning OFF - Log Usage
            if (device.getLastStartTime() != null) {
                long minutes = Duration.between(device.getLastStartTime(), now).toMinutes();

                // Prevent negative or zero duration if toggled too fast (minimum 1 min for
                // non-zero calc if needed, or allow 0)
                if (minutes > 0) {
                    // Formula: (Power * Minutes) / 60000.0 (User provided formula seems to be P *
                    // MIN / 60000)
                    // wait, (P * MIN) / 60 -> Watt-Hours. / 1000 -> kWh.
                    // So (P * MIN) / 60000.0 is correct for kWh.
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
            double currentLoad = calculateCurrentLoad(owner.getId());
            double potentialLoad = currentLoad + device.getPowerRating();

            if (potentialLoad > maxWattage) {
                double required = potentialLoad - maxWattage;
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
            // Log usage before shedding
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
            if (shedAmount >= requiredWattage) {
                return true;
            }
        }

        return shedAmount >= requiredWattage;
    }

    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setType(device.getType());
        dto.setPowerRating(device.getPowerRating());
        dto.setStatus(device.getStatus());
        dto.setPriority(device.getPriority().name());
        return dto;
    }
}
