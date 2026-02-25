package com.smarthome.backend.repository;

import com.smarthome.backend.model.DeviceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceScheduleRepository extends JpaRepository<DeviceSchedule, Long> {
    List<DeviceSchedule> findByDeviceId(Long deviceId);

    List<DeviceSchedule> findByActiveTrue();

    void deleteByDeviceId(Long deviceId);
}
