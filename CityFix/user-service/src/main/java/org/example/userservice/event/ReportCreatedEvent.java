package org.example.userservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCreatedEvent {
    private Long reportId;
    private Long userId;
    private String title;
    private String status;
    private String category;
    private String priority;
    private LocalDateTime createdAt;
}

