package com.simul.post.application.port.in;

import com.simul.post.application.dto.FeedPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * 특정 사용자의 게시물을 가져오는 유즈케이스
 */
public interface GetUserPostsUseCase {
    /**
     * 특정 유저의 게시물 목록 조회
     * @param targetUserId 조회 대상 유저 ID
     * @param currentUserId 현재 로그인한 유저 ID (비공개 게시물 노출 판단용)
     * @param pageable 페이지네이션 정보
     * @return 게시물 페이지
     */
    Page<FeedPostResponse> getUserPosts(UUID targetUserId, UUID currentUserId, Pageable pageable);

    /**
     * 특정 유저의 전체 게시물 수 조회
     * @param userId 유저 ID
     * @return 게시물 수
     */
    long countUserPosts(UUID userId);

    /**
     * 특정 유저가 좋아요한 게시물 목록 조회
     * @param userId 유저 ID
     * @param pageable 페이지네이션 정보
     * @return 게시물 페이지
     */
    Page<FeedPostResponse> getLikedPosts(UUID userId, Pageable pageable);
}
