package org.example.reportservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private String eventType;
    private Long userId;
    private String username;
    private String entityType;
    private Long entityId;
    private String action;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
}

