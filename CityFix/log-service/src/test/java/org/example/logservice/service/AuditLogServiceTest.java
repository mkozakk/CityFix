package org.example.logservice.service;

import org.example.logservice.entity.AuditLog;
import org.example.logservice.event.AuditEvent;
import org.example.logservice.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService(auditLogRepository);
    }

    @Test
    void testLogEventSuccess() {
        AuditEvent event = AuditEvent.builder()
                .eventType("USER")
                .userId(1L)
                .username("testuser")
                .entityType("User")
                .entityId(1L)
                .action("login")
                .details("User logged in")
                .ipAddress("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog savedLog = AuditLog.builder()
                .id(1L)
                .eventType("USER")
                .userId(1L)
                .username("testuser")
                .entityType("User")
                .entityId(1L)
                .action("login")
                .details("User logged in")
                .ipAddress("192.168.1.1")
                .createdAt(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.logEvent(event);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogEventWithoutTimestamp() {
        AuditEvent event = AuditEvent.builder()
                .eventType("REPORT")
                .userId(2L)
                .username("reportuser")
                .entityType("Report")
                .entityId(5L)
                .action("create")
                .details("Report created")
                .timestamp(null)
                .build();

        AuditLog savedLog = AuditLog.builder()
                .id(2L)
                .eventType("REPORT")
                .userId(2L)
                .username("reportuser")
                .entityType("Report")
                .entityId(5L)
                .action("create")
                .details("Report created")
                .createdAt(LocalDateTime.now())
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);

        auditLogService.logEvent(event);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testGetAllLogsSuccess() {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .eventType("USER")
                .userId(1L)
                .username("user1")
                .action("login")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .eventType("REPORT")
                .userId(2L)
                .username("user2")
                .action("create")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        Pageable pageable = PageRequest.of(0, 50);
        Page<AuditLog> page = new PageImpl<>(Arrays.asList(log1, log2));

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        List<AuditLog> logs = auditLogService.getAllLogs(50);

        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertEquals("USER", logs.get(0).getEventType());
        assertEquals("REPORT", logs.get(1).getEventType());
        verify(auditLogRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void testGetAllLogsEmpty() {
        Pageable pageable = PageRequest.of(0, 50);
        Page<AuditLog> emptyPage = new PageImpl<>(Arrays.asList());

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

        List<AuditLog> logs = auditLogService.getAllLogs(50);

        assertNotNull(logs);
        assertEquals(0, logs.size());
        verify(auditLogRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void testGetAllLogsWithCustomLimit() {
        AuditLog log = AuditLog.builder()
                .id(1L)
                .eventType("USER")
                .userId(1L)
                .username("testuser")
                .action("login")
                .createdAt(LocalDateTime.now())
                .build();

        Pageable pageable = PageRequest.of(0, 100);
        Page<AuditLog> page = new PageImpl<>(Arrays.asList(log));

        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        List<AuditLog> logs = auditLogService.getAllLogs(100);

        assertNotNull(logs);
        assertEquals(1, logs.size());
        verify(auditLogRepository).findAllByOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void testGetLogsByUserIdSuccess() {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .userId(1L)
                .username("user1")
                .eventType("USER")
                .action("login")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .userId(1L)
                .username("user1")
                .eventType("REPORT")
                .action("create")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(log1, log2));

        List<AuditLog> logs = auditLogService.getLogsByUserId(1L);

        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> log.getUserId().equals(1L)));
        verify(auditLogRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetLogsByUserIdEmpty() {
        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(999L))
                .thenReturn(Arrays.asList());

        List<AuditLog> logs = auditLogService.getLogsByUserId(999L);

        assertNotNull(logs);
        assertEquals(0, logs.size());
        verify(auditLogRepository).findByUserIdOrderByCreatedAtDesc(999L);
    }

    @Test
    void testGetLogsByUserIdMultipleRecords() {
        List<AuditLog> userLogs = Arrays.asList(
                AuditLog.builder().id(1L).userId(1L).eventType("USER").createdAt(LocalDateTime.now()).build(),
                AuditLog.builder().id(2L).userId(1L).eventType("REPORT").createdAt(LocalDateTime.now().minusMinutes(10)).build(),
                AuditLog.builder().id(3L).userId(1L).eventType("USER").createdAt(LocalDateTime.now().minusHours(1)).build()
        );

        when(auditLogRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(userLogs);

        List<AuditLog> logs = auditLogService.getLogsByUserId(1L);

        assertNotNull(logs);
        assertEquals(3, logs.size());
        verify(auditLogRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetLogsByEventTypeSuccess() {
        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .eventType("USER")
                .userId(1L)
                .username("user1")
                .action("login")
                .createdAt(LocalDateTime.now())
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .eventType("USER")
                .userId(2L)
                .username("user2")
                .action("register")
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        when(auditLogRepository.findByEventTypeOrderByCreatedAtDesc("USER"))
                .thenReturn(Arrays.asList(log1, log2));

        List<AuditLog> logs = auditLogService.getLogsByEventType("USER");

        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> log.getEventType().equals("USER")));
        verify(auditLogRepository).findByEventTypeOrderByCreatedAtDesc("USER");
    }

    @Test
    void testGetLogsByEventTypeEmpty() {
        when(auditLogRepository.findByEventTypeOrderByCreatedAtDesc("NONEXISTENT"))
                .thenReturn(Arrays.asList());

        List<AuditLog> logs = auditLogService.getLogsByEventType("NONEXISTENT");

        assertNotNull(logs);
        assertEquals(0, logs.size());
        verify(auditLogRepository).findByEventTypeOrderByCreatedAtDesc("NONEXISTENT");
    }

    @Test
    void testGetLogsByEventTypeMultipleRecords() {
        List<AuditLog> reportLogs = Arrays.asList(
                AuditLog.builder().id(1L).eventType("REPORT").userId(1L).action("create").createdAt(LocalDateTime.now()).build(),
                AuditLog.builder().id(2L).eventType("REPORT").userId(2L).action("update").createdAt(LocalDateTime.now().minusMinutes(30)).build(),
                AuditLog.builder().id(3L).eventType("REPORT").userId(3L).action("delete").createdAt(LocalDateTime.now().minusHours(2)).build()
        );

        when(auditLogRepository.findByEventTypeOrderByCreatedAtDesc("REPORT")).thenReturn(reportLogs);

        List<AuditLog> logs = auditLogService.getLogsByEventType("REPORT");

        assertNotNull(logs);
        assertEquals(3, logs.size());
        verify(auditLogRepository).findByEventTypeOrderByCreatedAtDesc("REPORT");
    }

    @Test
    void testGetLogsByDateRangeSuccess() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        AuditLog log1 = AuditLog.builder()
                .id(1L)
                .eventType("USER")
                .userId(1L)
                .username("user1")
                .action("login")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        AuditLog log2 = AuditLog.builder()
                .id(2L)
                .eventType("REPORT")
                .userId(2L)
                .username("user2")
                .action("create")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate))
                .thenReturn(Arrays.asList(log1, log2));

        List<AuditLog> logs = auditLogService.getLogsByDateRange(startDate, endDate);

        assertNotNull(logs);
        assertEquals(2, logs.size());
        verify(auditLogRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    @Test
    void testGetLogsByDateRangeEmpty() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now().minusDays(20);

        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate))
                .thenReturn(Arrays.asList());

        List<AuditLog> logs = auditLogService.getLogsByDateRange(startDate, endDate);

        assertNotNull(logs);
        assertEquals(0, logs.size());
        verify(auditLogRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    @Test
    void testGetLogsByDateRangeMultipleRecords() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now();

        List<AuditLog> logs = Arrays.asList(
                AuditLog.builder().id(1L).eventType("USER").createdAt(LocalDateTime.now().minusDays(5)).build(),
                AuditLog.builder().id(2L).eventType("REPORT").createdAt(LocalDateTime.now().minusDays(3)).build(),
                AuditLog.builder().id(3L).eventType("USER").createdAt(LocalDateTime.now().minusDays(1)).build()
        );

        when(auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate))
                .thenReturn(logs);

        List<AuditLog> result = auditLogService.getLogsByDateRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(3, result.size());
        verify(auditLogRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
}
