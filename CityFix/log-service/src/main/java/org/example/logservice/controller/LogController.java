package org.example.logservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.logservice.entity.AuditLog;
import org.example.logservice.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/logs")
public class LogController {
    private final AuditLogService auditLogService;

    @Value("${log-service.access-password}")
    private String accessPassword;

    @Autowired
    public LogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<?> getLogs(
            @RequestParam(required = false) String password,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String eventType) {

        // param password auth set via env var
        if (password == null || !password.equals(accessPassword)) {
            log.warn("Unauthorized access attempt to logs");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid password");
        }

        log.info("Authorized access to logs: limit={}, userId={}, eventType={}", limit, userId, eventType);

        try {
            List<AuditLog> logs;

            if (userId != null) {
                logs = auditLogService.getLogsByUserId(userId);
            } else if (eventType != null) {
                logs = auditLogService.getLogsByEventType(eventType);
            } else {
                logs = auditLogService.getAllLogs(limit);
            }

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            log.error("Error fetching logs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching logs");
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Log Service is running");
    }
}

