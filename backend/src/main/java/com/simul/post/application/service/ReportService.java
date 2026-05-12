package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.port.in.CreateNotificationUseCase;
import com.simul.notification.domain.model.NotificationType;
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
    private final CreateNotificationUseCase createNotificationUseCase;

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

        boolean justBlinded = post.incrementReportCount();
        postRepositoryPort.save(post);

        // 블라인드가 이번 신고로 발동되었으면 작성자에게 알림 전송
        if (justBlinded) {
            createNotificationUseCase.createNotification(
                    CreateNotificationUseCase.CreateNotificationCommand.builder()
                            .actorId(null) // 시스템 알림이므로 actor 없음
                            .recipientId(post.getUserId())
                            .type(NotificationType.REPORT_BLIND)
                            .referenceId(postId)
                            .message("회원님의 게시물이 커뮤니티 가이드 위반 신고 누적으로 블라인드 처리되었습니다.")
                            .build()
            );
        }
    }
}
