package com.simul.post.application.port.out;

import com.simul.post.domain.model.PostReport;
import java.util.UUID;

public interface PostReportPersistencePort {
    PostReport save(PostReport report);
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    int countByPostId(UUID postId);
}
