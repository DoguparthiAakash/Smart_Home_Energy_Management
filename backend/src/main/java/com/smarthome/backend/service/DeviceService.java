package com.smarthome.backend.service;

import com.smarthome.backend.dto.DeviceDTO;
import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    // Default Limit (Simple implementation, ideally stored in DB per user)
    private static Double MAX_WATTAGE = 5000.0;

    public void setMaxWattage(Double limit) {
        MAX_WATTAGE = limit;
    }

    public Double getMaxWattage() {
        return MAX_WATTAGE;
    }

    public List<DeviceDTO> getDevicesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
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
        // Default to false if null
        device.setIsCritical(deviceDTO.getIsCritical() != null ? deviceDTO.getIsCritical() : false);

        Device saved = deviceRepository.save(device);
        return convertToDTO(saved);
    }

    public DeviceDTO toggleDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        boolean newStatus = !device.getStatus();

        // If turning ON, check limit FIRST
        if (newStatus) {
            checkAndEnforceLimit(device);
        } else {
            device.setStatus(false);
            deviceRepository.save(device);
        }

        return convertToDTO(deviceRepository.findById(deviceId).orElse(device));
    }

    // Logic to enforce limit
    private void checkAndEnforceLimit(Device targetDevice) {
        List<Device> userDevices = deviceRepository.findByUserId(targetDevice.getUser().getId());

        // Calculate potential total
        double currentTotal = userDevices.stream()
                .filter(Device::getStatus)
                .mapToDouble(Device::getPowerRating)
                .sum();

        double potentialTotal = currentTotal + targetDevice.getPowerRating();

        if (potentialTotal > MAX_WATTAGE) {
            // Strategy: Try to turn on target, but shut off others if needed.
            // OR if target is Critical, force it on and kill others.
            // OR if target is Non-Critical and limit reached, prevent it?
            // User requested: "Turn off if it exceeds... and avoider... to not shut down".

            // Logic: Allow turning ON, then shed load from Non-Critical devices to get back
            // under limit.

            targetDevice.setStatus(true); // Temporarily assume ON
            userDevices.add(targetDevice); // Ensure list has updated state conceptually

            // Re-fetch or use list (targetDevice is in userDevices if it was already saved,
            // but status might be old in list).
            // Let's modify the list in memory for calculation.
            for (Device d : userDevices) {
                if (d.getId().equals(targetDevice.getId()))
                    d.setStatus(true);
            }

            double total = userDevices.stream().filter(Device::getStatus).mapToDouble(Device::getPowerRating).sum();

            if (total > MAX_WATTAGE) {
                // Sort ON devices: Non-Critical first, then by Power Rating Descending (shed
                // biggest loads first)
                List<Device> candidates = userDevices.stream()
                        .filter(Device::getStatus)
                        .filter(d -> !d.getIsCritical()) // protect critical
                        .sorted((d1, d2) -> Double.compare(d2.getPowerRating(), d1.getPowerRating()))
                        .collect(Collectors.toList());

                for (Device d : candidates) {
                    if (total <= MAX_WATTAGE)
                        break;
                    // Don't turn off the device we just tried to turn on IF it is critical?
                    // No, if target is non-critical, it might just be denied.
                    // But if target is Critical, we must shed others.

                    if (d.getId().equals(targetDevice.getId()) && targetDevice.getIsCritical())
                        continue;

                    d.setStatus(false);
                    deviceRepository.save(d); // Persist shutoff
                    total -= d.getPowerRating();
                }

                // If still over limit (e.g. only critical devices left), we might have to deny
                // the target if it's not critical
                if (total > MAX_WATTAGE && !targetDevice.getIsCritical()) {
                    targetDevice.setStatus(false); // Deny
                    // Throw exception or just save false?
                    // Verify: The user wants "Avoider" to NOT shut down.
                }
            }
            deviceRepository.save(targetDevice);

        } else {
            targetDevice.setStatus(true);
            deviceRepository.save(targetDevice);
        }
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
        dto.setIsCritical(device.getIsCritical());
        dto.setMqttTopic(device.getMqttTopic());
        return dto;
    }

    public List<Device> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    // --- Advanced Provisioning ---

    public DeviceDTO pairDevice(String userEmail, String pairingCode) {
        // Format: TYPE|NAME|POWER|TOPIC|CRITICAL
        // Example: AC|Living Room AC|1500|home/living/ac|false
        try {
            String[] parts = pairingCode.split("\\|");
            if (parts.length < 3)
                throw new RuntimeException("Invalid Code Format");

            DeviceDTO dto = new DeviceDTO();
            dto.setType(parts[0]);
            dto.setName(parts[1]);
            dto.setPowerRating(Double.parseDouble(parts[2]));
            if (parts.length > 3)
                dto.setMqttTopic(parts[3]);
            if (parts.length > 4)
                dto.setIsCritical(Boolean.parseBoolean(parts[4]));
            else
                dto.setIsCritical(false);

            dto.setStatus(false);
            return addDevice(userEmail, dto);
        } catch (Exception e) {
            throw new RuntimeException("Pairing failed: " + e.getMessage());
        }
    }

    public List<DeviceDTO> getDiscoveredDevices() {
        // Simulated Discovery
        // Simulated Samsung Device Discovery
        List<DeviceDTO> found = new java.util.ArrayList<>();

        DeviceDTO d1 = new DeviceDTO();
        d1.setName("Samsung WindFree™ AC");
        d1.setType("AC");
        d1.setPowerRating(1200.0);
        d1.setMqttTopic("samsung/ac/windfree");
        found.add(d1);

        DeviceDTO d2 = new DeviceDTO();
        d2.setName("Samsung 4-Door Flex™ Fridge");
        d2.setType("FRIDGE");
        d2.setPowerRating(250.0);
        d2.setMqttTopic("samsung/fridge/flex");
        found.add(d2);

        DeviceDTO d3 = new DeviceDTO();
        d3.setName("Samsung Jet Bot™ AI+");
        d3.setType("OTHER");
        d3.setPowerRating(30.0);
        d3.setMqttTopic("samsung/robot/jetbot");
        found.add(d3);

        DeviceDTO d4 = new DeviceDTO();
        d4.setName("Samsung Neo QLED 8K TV");
        d4.setType("TV"); // Assuming TV type or OTHER
        d4.setPowerRating(180.0);
        d4.setMqttTopic("samsung/tv/qled");
        found.add(d4);

        return found;
    }
}
