package org.example.reportservice.service;

import org.example.reportservice.dto.CreateReportRequest;
import org.example.reportservice.dto.ReportResponse;
import org.example.reportservice.dto.UpdateReportRequest;
import org.example.reportservice.entity.Report;
import org.example.reportservice.messaging.AuditEventPublisher;
import org.example.reportservice.messaging.ReportEventPublisher;
import org.example.reportservice.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEventPublisher eventPublisher;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, eventPublisher, auditEventPublisher);
    }

    @Test
    void testCreateReportSuccess() {
        CreateReportRequest request = CreateReportRequest.builder()
                .title("Broken street light")
                .description("Street light at Main St is not working")
                .category("INFRASTRUCTURE")
                .priority("MEDIUM")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        Report savedReport = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Broken street light")
                .description("Street light at Main St is not working")
                .category("INFRASTRUCTURE")
                .priority("MEDIUM")
                .status("OPEN")
                .latitude(51.5074)
                .longitude(-0.1278)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        ReportResponse response = reportService.createReport(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Broken street light", response.getTitle());
        assertEquals("INFRASTRUCTURE", response.getCategory());
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("OPEN", response.getStatus());
        verify(reportRepository).save(any(Report.class));
        verify(eventPublisher).publishReportCreated(any());
        verify(auditEventPublisher).publishAudit(eq("report.create"), any());
    }

    @Test
    void testCreateReportWithDefaultPriority() {
        CreateReportRequest request = CreateReportRequest.builder()
                .title("Pothole")
                .description("Large pothole on Main St")
                .category("ROADS")
                .latitude(52.2297)
                .longitude(21.0122)
                .build();

        Report savedReport = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Pothole")
                .description("Large pothole on Main St")
                .category("ROADS")
                .priority("MEDIUM")
                .status("OPEN")
                .latitude(52.2297)
                .longitude(21.0122)
                .createdAt(LocalDateTime.now())
                .build();

        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        ReportResponse response = reportService.createReport(request, 1L);

        assertNotNull(response);
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("OPEN", response.getStatus());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testGetAllReportsSuccess() {
        Report report1 = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Report 1")
                .category("INFRASTRUCTURE")
                .status("OPEN")
                .build();

        Report report2 = Report.builder()
                .id(2L)
                .userId(2L)
                .title("Report 2")
                .category("ROADS")
                .status("CLOSED")
                .build();

        when(reportRepository.findAll()).thenReturn(Arrays.asList(report1, report2));

        List<ReportResponse> responses = reportService.getAllReports();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Report 1", responses.get(0).getTitle());
        assertEquals("Report 2", responses.get(1).getTitle());
        verify(reportRepository).findAll();
    }

    @Test
    void testGetAllReportsEmpty() {
        when(reportRepository.findAll()).thenReturn(Arrays.asList());

        List<ReportResponse> responses = reportService.getAllReports();

        assertNotNull(responses);
        assertEquals(0, responses.size());
        verify(reportRepository).findAll();
    }

    @Test
    void testGetReportByIdSuccess() {
        Report report = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Broken street light")
                .description("Street light at Main St is not working")
                .category("INFRASTRUCTURE")
                .priority("MEDIUM")
                .status("OPEN")
                .latitude(51.5074)
                .longitude(-0.1278)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        ReportResponse response = reportService.getReportById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Broken street light", response.getTitle());
        verify(reportRepository).findById(1L);
    }

    @Test
    void testGetReportByIdNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reportService.getReportById(999L));
        verify(reportRepository).findById(999L);
    }

    @Test
    void testUpdateReportSuccess() {
        Report existingReport = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Original Title")
                .description("Original Description")
                .category("INFRASTRUCTURE")
                .priority("MEDIUM")
                .status("OPEN")
                .build();

        UpdateReportRequest request = UpdateReportRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .status("IN_PROGRESS")
                .priority("HIGH")
                .build();

        Report updatedReport = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Updated Title")
                .description("Updated Description")
                .category("INFRASTRUCTURE")
                .priority("HIGH")
                .status("IN_PROGRESS")
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));
        when(reportRepository.save(any(Report.class))).thenReturn(updatedReport);

        ReportResponse response = reportService.updateReport(1L, request, 1L);

        assertNotNull(response);
        assertEquals("Updated Title", response.getTitle());
        assertEquals("Updated Description", response.getDescription());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("HIGH", response.getPriority());
        verify(reportRepository).findById(1L);
        verify(reportRepository).save(any(Report.class));
        verify(auditEventPublisher).publishAudit(eq("report.update"), any());
    }

    @Test
    void testUpdateReportUnauthorized() {
        Report existingReport = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Original Title")
                .build();

        UpdateReportRequest request = UpdateReportRequest.builder()
                .title("Updated Title")
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(existingReport));

        assertThrows(SecurityException.class, () -> reportService.updateReport(1L, request, 2L));
        verify(reportRepository).findById(1L);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void testUpdateReportNotFound() {
        UpdateReportRequest request = UpdateReportRequest.builder()
                .title("Updated Title")
                .build();

        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reportService.updateReport(999L, request, 1L));
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).save(any());
    }

    @Test
    void testDeleteReportSuccess() {
        Report report = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Report to Delete")
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        reportService.deleteReport(1L, 1L);

        verify(reportRepository).findById(1L);
                verify(reportRepository).deleteById(1L);
        verify(auditEventPublisher).publishAudit(eq("report.delete"), any());
    }

    @Test
    void testDeleteReportUnauthorized() {
        Report report = Report.builder()
                .id(1L)
                .userId(1L)
                .title("Report to Delete")
                .build();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThrows(SecurityException.class, () -> reportService.deleteReport(1L, 2L));
        verify(reportRepository).findById(1L);
        verify(reportRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteReportNotFound() {
        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reportService.deleteReport(999L, 1L));
        verify(reportRepository).findById(999L);
        verify(reportRepository, never()).deleteById(any());
    }
}
