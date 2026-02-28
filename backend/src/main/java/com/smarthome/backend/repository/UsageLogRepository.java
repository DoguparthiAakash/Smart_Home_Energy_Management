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
        List<UsageLog> findByUserIdAndTimestampBetween(@Param("userId") Long userId,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT SUM(u.energyKwh) FROM UsageLog u WHERE u.timestamp BETWEEN :start AND :end")
        Double sumEnergyBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        @Query("SELECT u FROM UsageLog u WHERE (:userId IS NULL OR u.device.user.id = :userId) AND (:deviceId IS NULL OR u.device.id = :deviceId) AND (:category IS NULL OR u.device.type = :category) AND u.timestamp BETWEEN :start AND :end")
        List<UsageLog> findByFilters(@Param("userId") Long userId,
                        @Param("deviceId") Long deviceId,
                        @Param("category") String category,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT d.name, SUM(u.energyKwh) FROM UsageLog u JOIN u.device d WHERE d.user.id = :userId AND u.timestamp BETWEEN :start AND :end GROUP BY d.name")
        List<Object[]> findUsagePerDevice(@Param("userId") Long userId, @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        void deleteByDeviceId(Long deviceId);
}
