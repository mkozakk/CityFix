package org.example.reportservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequest {
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status;

    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @Size(max = 50, message = "Priority must be less than 50 characters")
    private String priority;

    private Double latitude;

    private Double longitude;
}

