package org.example.logservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.logservice.event.AuditEvent;
import org.example.logservice.service.AuditLogService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventListener {
    private final AuditLogService auditLogService;

    @Autowired
    public AuditEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.audit-logs:audit.logs.queue}")
    public void handleAuditEvent(
            AuditEvent event,
            @Header(value = "amqp_messageId", required = false) String messageId) {

        log.info("Received AuditEvent: type={}, user={}, action={}, messageId={}",
                event.getEventType(), event.getUsername(), event.getAction(), messageId);

        try {
            auditLogService.logEvent(event);
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage(), e);
            throw e; // Retry przez RabbitMQ
        }
    }
}

