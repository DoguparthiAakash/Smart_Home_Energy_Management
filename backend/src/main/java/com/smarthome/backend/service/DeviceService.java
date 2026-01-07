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

        Device saved = deviceRepository.save(device);
        return convertToDTO(saved);
    }

    public DeviceDTO toggleDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new RuntimeException("Device not found"));
        device.setStatus(!device.getStatus());
        return convertToDTO(deviceRepository.save(device));
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
        return dto;
    }

    public List<Device> getDevicesByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }
}
