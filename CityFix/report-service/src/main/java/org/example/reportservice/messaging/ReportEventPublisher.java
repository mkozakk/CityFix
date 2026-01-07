package org.example.reportservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.reportservice.event.ReportCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.reports:cityfix.reports}")
    private String reportsExchange;

    @Value("${rabbitmq.routing-key.report-created:report.created}")
    private String reportCreatedRoutingKey;

    @Autowired
    public ReportEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReportCreated(ReportCreatedEvent event) {
        try {
            log.info("Publishing ReportCreatedEvent for report ID: {}", event.getReportId());
            rabbitTemplate.convertAndSend(reportsExchange, reportCreatedRoutingKey, event);
            log.info("Successfully published ReportCreatedEvent for report ID: {}", event.getReportId());
        } catch (Exception e) {
            log.error("Failed to publish ReportCreatedEvent for report ID: {}", event.getReportId(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

