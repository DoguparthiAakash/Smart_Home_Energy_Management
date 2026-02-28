package com.smarthome.backend.repository;

import com.smarthome.backend.model.TechnicianVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface TechnicianVisitRepository extends JpaRepository<TechnicianVisit, Long> {
    List<LocalDate> findDistinctVisitDateByOrderByVisitDateDesc();
    List<TechnicianVisit> findByVisitDateBetweenOrderByVisitDateDescStartTimeDesc(LocalDate start, LocalDate end);
    List<TechnicianVisit> findTop10ByOrderByStartTimeDesc();
}
