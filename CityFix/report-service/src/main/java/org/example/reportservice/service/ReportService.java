package org.example.reportservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.reportservice.dto.CreateReportRequest;
import org.example.reportservice.dto.ReportResponse;
import org.example.reportservice.dto.UpdateReportRequest;
import org.example.reportservice.entity.Report;
import org.example.reportservice.event.AuditEvent;
import org.example.reportservice.event.ReportCreatedEvent;
import org.example.reportservice.messaging.AuditEventPublisher;
import org.example.reportservice.messaging.ReportEventPublisher;
import org.example.reportservice.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportEventPublisher eventPublisher;
    private final AuditEventPublisher auditEventPublisher;

    @Autowired
    public ReportService(ReportRepository reportRepository,
                        ReportEventPublisher eventPublisher,
                        AuditEventPublisher auditEventPublisher) {
        this.reportRepository = reportRepository;
        this.eventPublisher = eventPublisher;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request, Long userId) {
        log.info("Creating new report for user ID: {}", userId);

        Report report = Report.builder()
                .userId(userId)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .status("OPEN")
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report created with ID: {}", savedReport.getId());

        // Publish event to RabbitMQ
        ReportCreatedEvent event = ReportCreatedEvent.builder()
                .reportId(savedReport.getId())
                .userId(savedReport.getUserId())
                .title(savedReport.getTitle())
                .status(savedReport.getStatus())
                .category(savedReport.getCategory())
                .priority(savedReport.getPriority())
                .createdAt(savedReport.getCreatedAt())
                .build();

        eventPublisher.publishReportCreated(event);

        // Publish audit event
        publishAudit("report.create", userId, null, "Report", savedReport.getId(),
                    "Report created: " + savedReport.getTitle());

        return mapToResponse(savedReport);
    }

    public List<ReportResponse> getAllReports() {
        log.info("Getting all reports");
        return reportRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReportResponse getReportById(Long id) {
        log.info("Getting report with ID: {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));
        return mapToResponse(report);
    }

    @Transactional
    public ReportResponse updateReport(Long id, UpdateReportRequest request, Long authenticatedUserId) {
        log.info("Updating report ID: {} by user ID: {}", id, authenticatedUserId);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        // Check ownership
        if (!report.getUserId().equals(authenticatedUserId)) {
            log.warn("User {} attempted to update report {} owned by user {}",
                    authenticatedUserId, id, report.getUserId());
            throw new SecurityException("You can only update your own reports");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }
        if (request.getCategory() != null) {
            report.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            report.setPriority(request.getPriority());
        }
        if (request.getLatitude() != null) {
            report.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            report.setLongitude(request.getLongitude());
        }

        Report updatedReport = reportRepository.save(report);
        log.info("Report ID: {} successfully updated", id);

        // Publish audit event
        publishAudit("report.update", authenticatedUserId, null, "Report", updatedReport.getId(),
                    "Report updated: " + updatedReport.getTitle());

        return mapToResponse(updatedReport);
    }

    @Transactional
    public void deleteReport(Long id, Long authenticatedUserId) {
        log.info("Deleting report ID: {} by user ID: {}", id, authenticatedUserId);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found with id: " + id));

        // Check ownership
        if (!report.getUserId().equals(authenticatedUserId)) {
            log.warn("User {} attempted to delete report {} owned by user {}",
                    authenticatedUserId, id, report.getUserId());
            throw new SecurityException("You can only delete your own reports");
        }

        String reportTitle = report.getTitle();
        reportRepository.deleteById(id);
        log.info("Report ID: {} successfully deleted", id);

        // Publish audit event
        publishAudit("report.delete", authenticatedUserId, null, "Report", id,
                    "Report deleted: " + reportTitle);
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .userId(report.getUserId())
                .title(report.getTitle())
                .description(report.getDescription())
                .status(report.getStatus())
                .category(report.getCategory())
                .priority(report.getPriority())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private void publishAudit(String action, Long userId, String username,
                             String entityType, Long entityId, String details) {
        try {
            AuditEvent event = AuditEvent.builder()
                    .eventType("REPORT")
                    .userId(userId)
                    .username(username)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .details(details)
                    .timestamp(LocalDateTime.now())
                    .build();
            auditEventPublisher.publishAudit(action, event);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", e.getMessage());
        }
    }
}

