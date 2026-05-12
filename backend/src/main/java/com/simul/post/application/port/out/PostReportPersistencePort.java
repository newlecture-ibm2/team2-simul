package com.simul.post.application.port.out;

import com.simul.post.domain.model.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PostReportPersistencePort {
    PostReport save(PostReport report);
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    int countByPostId(UUID postId);
    Page<PostReport> loadAllReports(Pageable pageable);
}
