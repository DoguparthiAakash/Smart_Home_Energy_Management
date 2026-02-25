package com.smarthome.backend.repository;

import com.smarthome.backend.model.DeviceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceEventRepository extends JpaRepository<DeviceEvent, Long> {
    List<DeviceEvent> findByDeviceIdOrderByTimestampDesc(Long deviceId);

    void deleteByDeviceId(Long deviceId);
}
