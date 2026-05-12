package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.port.in.ReportPostUseCase;
import com.simul.post.application.port.out.ReportPersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements ReportPostUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final ReportPersistencePort reportPersistencePort;

    @Override
    @Transactional
    public void reportPost(UUID postId, UUID reporterId, String reason) {
        Post post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (reportPersistencePort.existsByPostIdAndReporterId(postId, reporterId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .postId(postId)
                .reporterId(reporterId)
                .reason(reason)
                .build();
        reportPersistencePort.save(report);

        post.incrementReportCount();
        postRepositoryPort.save(post);
    }
}
