package com.smarthome.backend.repository;

import com.smarthome.backend.model.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    List<UsageLog> findByDeviceIdAndTimestampBetween(Long deviceId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT u FROM UsageLog u JOIN u.device d WHERE d.user.id = :userId AND u.timestamp BETWEEN :start AND :end")
    List<UsageLog> findByUserIdAndTimestampBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
