package com.smarthome.backend.service;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

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

        // If turning ON, check load
        if (!device.getStatus()) {
            User owner = device.getUser();
            double maxWattage = owner.getMaxWattage() != null ? owner.getMaxWattage() : 5000.0;
            double currentLoad = calculateCurrentLoad(owner.getId());
            double potentialLoad = currentLoad + device.getPowerRating();

            if (potentialLoad > maxWattage) {
                double required = potentialLoad - maxWattage;
                System.out.println("LOAD LIMIT EXCEEDED! Current: " + currentLoad + " + Device: "
                        + device.getPowerRating() + " > Max: " + maxWattage + ". Shedding: " + required);
                boolean success = shedLoad(owner.getId(), required);
                if (!success) {
                    throw new RuntimeException(
                            "Cannot turn on device. Max wattage exceeded and no lower priority devices to shed.");
                }
            }
        }

        device.setStatus(!device.getStatus());
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

        // Sort by Priority: LOW first, then MEDIUM. (High is skipped or last)
        // High = 0, Medium = 1, Low = 2 in natural enum order? No, define explicit
        // order.
        // Priority: HIGH, MEDIUM, LOW.
        // We want to shed LOW first.

        List<Device> shedCandidates = activeDevices.stream()
                .filter(d -> d.getPriority() != Device.Priority.HIGH) // Never shed HIGH
                .sorted((d1, d2) -> {
                    // Priority.LOW (2) > Priority.MEDIUM (1). We want LOW first.
                    return d2.getPriority().compareTo(d1.getPriority());
                })
                .collect(Collectors.toList());

        double shedAmount = 0;

        for (Device d : shedCandidates) {
            d.setStatus(false);
            deviceRepository.save(d);
            shedAmount += d.getPowerRating();
            System.out.println("SHEDDING LOAD: Turned off " + d.getName() + " (" + d.getPriority() + ") - "
                    + d.getPowerRating() + "W");

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
