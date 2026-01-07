package org.example.reportservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.reportservice.dto.CreateReportRequest;
import org.example.reportservice.dto.ReportResponse;
import org.example.reportservice.dto.UpdateReportRequest;
import org.example.reportservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody CreateReportRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Creating report for user ID: {}", userId);

        if (userId == null) {
            log.warn("Unauthorized create report attempt - missing JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ReportResponse response = reportService.createReport(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create report: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        log.info("Getting all reports");
        List<ReportResponse> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        log.info("Getting report with ID: {}", id);
        try {
            ReportResponse response = reportService.getReportById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Report not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Updating report ID: {} by user ID: {}", id, userId);

        if (userId == null) {
            log.warn("Unauthorized update attempt - missing JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            ReportResponse response = reportService.updateReport(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            log.warn("Forbidden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Report not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Deleting report ID: {} by user ID: {}", id, userId);

        if (userId == null) {
            log.warn("Unauthorized delete attempt - missing JWT token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            reportService.deleteReport(id, userId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            log.warn("Forbidden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            log.error("Report not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Report Service is running");
    }
}

