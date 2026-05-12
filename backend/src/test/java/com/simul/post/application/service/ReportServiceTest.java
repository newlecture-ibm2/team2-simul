package com.simul.post.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.notification.application.dto.ReportBlindedEvent;
import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.application.port.out.ReportPersistencePort;
import com.simul.post.domain.model.Post;
import com.simul.post.domain.model.PostStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PostRepositoryPort postRepositoryPort;

    @Mock
    private ReportPersistencePort reportPersistencePort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportService reportService;

    private UUID postId;
    private UUID reporterId;
    private UUID postAuthorId;
    private Post post;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        reporterId = UUID.randomUUID();
        postAuthorId = UUID.randomUUID();

        post = Post.builder()
                .postId(postId)
                .userId(postAuthorId)
                .status(PostStatus.COMPLETED)
                .reportCount(0)
                .isBlinded(false)
                .build();
    }

    @Test
    @DisplayName("정상 신고 접수: 1~9회 누적 시 블라인드 및 알림 이벤트 미발행")
    void reportPost_success() {
        // given
        given(postRepositoryPort.findById(postId)).willReturn(Optional.of(post));
        given(reportPersistencePort.existsByPostIdAndReporterId(postId, reporterId)).willReturn(false);

        // when
        reportService.reportPost(postId, reporterId, "SPAM");

        // then
        assertThat(post.getReportCount()).isEqualTo(1);
        assertThat(post.getIsBlinded()).isFalse();
        
        verify(reportPersistencePort, times(1)).save(any());
        verify(postRepositoryPort, times(1)).save(post);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("중복 신고 방지: 예외 발생")
    void reportPost_duplicate() {
        // given
        given(postRepositoryPort.findById(postId)).willReturn(Optional.of(post));
        given(reportPersistencePort.existsByPostIdAndReporterId(postId, reporterId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> reportService.reportPost(postId, reporterId, "SPAM"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_REPORT);
    }

    @Test
    @DisplayName("10회 누적: 자동 블라인드 처리 및 작성자 알림 이벤트 발행")
    void reportPost_blindAndNotify() {
        // given
        // 미리 신고 9회 누적 상태로 세팅
        post = Post.builder()
                .postId(postId)
                .userId(postAuthorId)
                .status(PostStatus.COMPLETED)
                .reportCount(9)
                .isBlinded(false)
                .build();

        given(postRepositoryPort.findById(postId)).willReturn(Optional.of(post));
        given(reportPersistencePort.existsByPostIdAndReporterId(postId, reporterId)).willReturn(false);

        // when
        reportService.reportPost(postId, reporterId, "INAPPROPRIATE");

        // then
        assertThat(post.getReportCount()).isEqualTo(10);
        assertThat(post.getIsBlinded()).isTrue();

        verify(reportPersistencePort, times(1)).save(any());
        verify(postRepositoryPort, times(1)).save(post);

        // 알림 이벤트 발행 확인
        ArgumentCaptor<ReportBlindedEvent> captor = ArgumentCaptor.forClass(ReportBlindedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());

        ReportBlindedEvent event = captor.getValue();
        assertThat(event.postId()).isEqualTo(postId);
        assertThat(event.postOwnerId()).isEqualTo(postAuthorId);
    }
}
