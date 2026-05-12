package com.simul.post.application.dto;

import com.simul.post.domain.model.Report;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
    UUID reportId,
    UUID postId,
    UUID reporterId,
    String reason,
    LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getReportId(),
            report.getPostId(),
            report.getReporterId(),
            report.getReason(),
            report.getCreatedAt()
        );
    }
}
