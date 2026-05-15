package com.simul.post.application.dto;

import com.simul.post.domain.model.PostReport;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportResponse(
    UUID reportId,
    UUID postId,
    UUID reporterId,
    UUID reportedUserId,
    String reason,
    boolean isBlinded,
    LocalDateTime createdAt
) {
    public static ReportResponse from(PostReport report, UUID reportedUserId, boolean isBlinded) {
        return new ReportResponse(
            report.getReportId(),
            report.getPostId(),
            report.getReporterId(),
            reportedUserId,
            report.getReason(),
            isBlinded,
            report.getCreatedAt()
        );
    }
}
