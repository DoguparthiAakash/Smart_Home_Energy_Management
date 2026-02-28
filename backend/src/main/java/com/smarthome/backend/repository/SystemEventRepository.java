package com.smarthome.backend.repository;

import com.smarthome.backend.model.SystemEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SystemEventRepository extends JpaRepository<SystemEvent, Long> {
    List<SystemEvent> findTop20ByOrderByTimestampDesc();

    long countByType(String type);
}
