package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.post.application.dto.ReportResponse;
import com.simul.post.application.port.in.GetReportsUseCase;
import com.simul.post.application.port.in.ReportPostUseCase;
import com.simul.post.application.port.out.PostReportPersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostReport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService implements ReportPostUseCase, GetReportsUseCase {

    private final PostRepositoryPort postRepositoryPort;
    private final PostReportPersistencePort postReportPersistencePort;

    @Override
    @Transactional
    public void reportPost(UUID postId, UUID reporterId, String reason) {
        Post post = postRepositoryPort.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (postReportPersistencePort.existsByPostIdAndReporterId(postId, reporterId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        PostReport report = PostReport.builder()
                .postId(postId)
                .reporterId(reporterId)
                .reason(reason)
                .build();
        postReportPersistencePort.save(report);

        post.incrementReportCount();
        postRepositoryPort.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(Pageable pageable) {
        return postReportPersistencePort.loadAllReports(pageable)
                .map(ReportResponse::from);
    }
}
