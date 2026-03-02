package com.smarthome.backend.controller;

import com.smarthome.backend.model.TechnicianVisit;
import com.smarthome.backend.repository.TechnicianVisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/technician-visits")
public class TechnicianExportController {

        @Autowired
        private TechnicianVisitRepository visitRepository;

        @GetMapping("/export")
        public ResponseEntity<byte[]> exportVisits(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

                List<TechnicianVisit> visits = visitRepository.findByVisitDateBetweenOrderByVisitDateDescStartTimeDesc(
                                start,
                                end);

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
                StringBuilder csv = new StringBuilder(
                                "\"ID\",\"Technician Name\",\"Email\",\"Visit Date\",\"Start Time\",\"End Time\",\"Status\",\"Activity\"\n");
                for (TechnicianVisit v : visits) {
                        csv.append("\"").append(v.getId()).append("\",");
                        csv.append("\"").append(v.getTechnician().getName().replace("\"", "\"\"")).append("\",");
                        csv.append("\"").append(v.getTechnician().getEmail()).append("\",");
                        csv.append("\"").append(v.getVisitDate()).append("\",");
                        csv.append("\"").append(v.getStartTime() != null ? v.getStartTime().format(dtf) : "")
                                        .append("\",");
                        csv.append("\"").append(v.getEndTime() != null ? v.getEndTime().format(dtf) : "").append("\",");
                        csv.append("\"").append(v.getStatus()).append("\",");
                        csv.append("\"").append(v.getActivity() != null ? v.getActivity().replace("\"", "\"\"") : "")
                                        .append("\"\n");
                }

                byte[] csvBytes = csv.toString().getBytes();

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"technician_visits.csv\"")
                                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                                .body(csvBytes);
        }
}
