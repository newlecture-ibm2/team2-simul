package com.simul.post.application.port.out;

import com.simul.post.domain.model.Report;
import java.util.UUID;

public interface ReportPersistencePort {
    Report save(Report report);
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    int countByPostId(UUID postId);
}
