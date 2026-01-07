package org.example.reportservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.reportservice.event.AuditEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.audit:cityfix.audit}")
    private String auditExchange;

    @Autowired
    public AuditEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishAudit(String action, AuditEvent event) {
        try {
            String routingKey = "audit." + action;
            log.info("Publishing AuditEvent: {} for report {}", action, event.getEntityId());
            rabbitTemplate.convertAndSend(auditExchange, routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish AuditEvent: {}", e.getMessage(), e);
        }
    }
}

