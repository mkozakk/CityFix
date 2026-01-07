package org.example.logservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.logservice.entity.AuditLog;
import org.example.logservice.event.AuditEvent;
import org.example.logservice.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logEvent(AuditEvent event) {
        AuditLog auditLog = AuditLog.builder()
                .eventType(event.getEventType())
                .userId(event.getUserId())
                .username(event.getUsername())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .action(event.getAction())
                .details(event.getDetails())
                .ipAddress(event.getIpAddress())
                .createdAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log saved: {} - {} by user {}",
                event.getEventType(), event.getAction(), event.getUsername());
    }

    public List<AuditLog> getAllLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<AuditLog> page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.getContent();
    }

    public List<AuditLog> getLogsByUserId(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> getLogsByEventType(String eventType) {
        return auditLogRepository.findByEventTypeOrderByCreatedAtDesc(eventType);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}

