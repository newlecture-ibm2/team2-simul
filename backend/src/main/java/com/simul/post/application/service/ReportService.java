package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.dto.ReportBlindedEvent;
import com.simul.post.application.dto.ReportResponse;
import com.simul.post.application.port.in.GetReportsUseCase;
import com.simul.post.application.port.in.ReportPostUseCase;
import com.simul.post.application.port.out.PostReportPersistencePort;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostReport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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

        boolean justBlinded = post.incrementReportCount();
        postRepositoryPort.save(post);

        // 블라인드가 이번 신고로 발동되었으면 작성자에게 알림 이벤트 발행
        if (justBlinded) {
            eventPublisher.publishEvent(new ReportBlindedEvent(postId, post.getUserId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(Pageable pageable) {
        return postReportPersistencePort.loadAllReports(pageable)
                .map(ReportResponse::from);
    }
}
