package com.simul.user.adapter.in.web;

import com.simul.user.application.dto.FollowCountResponse;
import com.simul.user.application.port.in.FollowUserUseCase;
import com.simul.user.application.port.in.LoadFollowUseCase;
import com.simul.user.application.port.in.UnfollowUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 팔로우 API 컨트롤러
 *
 * 엔드포인트:
 * - POST /follows/{userId}             → 팔로우
 * - DELETE /follows/{userId}           → 언팔로우
 * - GET /users/{userId}/follow-counts  → 팔로워/팔로잉 수 조회
 * - GET /users/{userId}/is-following   → 팔로우 여부 확인
 */
@RestController
public class FollowController {

    private final FollowUserUseCase followUserUseCase;
    private final UnfollowUserUseCase unfollowUserUseCase;
    private final LoadFollowUseCase loadFollowUseCase;

    public FollowController(
        FollowUserUseCase followUserUseCase,
        UnfollowUserUseCase unfollowUserUseCase,
        LoadFollowUseCase loadFollowUseCase
    ) {
        this.followUserUseCase = followUserUseCase;
        this.unfollowUserUseCase = unfollowUserUseCase;
        this.loadFollowUseCase = loadFollowUseCase;
    }

    /**
     * 팔로우
     * POST /follows/{userId}
     */
    @PostMapping("/follows/{userId}")
    public ResponseEntity<Void> follow(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        followUserUseCase.follow(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 언팔로우
     * DELETE /follows/{userId}
     */
    @DeleteMapping("/follows/{userId}")
    public ResponseEntity<Void> unfollow(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        unfollowUserUseCase.unfollow(currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 팔로워/팔로잉 수 조회
     * GET /users/{userId}/follow-counts
     */
    @GetMapping("/users/{userId}/follow-counts")
    public ResponseEntity<FollowCountResponse> getFollowCounts(@PathVariable UUID userId) {
        FollowCountResponse response = loadFollowUseCase.getFollowCounts(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 팔로우 여부 확인
     * GET /users/{userId}/is-following
     * - 로그인한 사용자가 해당 사용자를 팔로우 중인지 확인
     */
    @GetMapping("/users/{userId}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        boolean following = loadFollowUseCase.isFollowing(currentUserId, userId);
        return ResponseEntity.ok(Map.of("isFollowing", following));
    }

    /**
     * 팔로워 목록 조회
     * GET /users/{userId}/followers
     */
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<java.util.List<com.simul.user.application.dto.FollowUserResponse>> getFollowers(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(loadFollowUseCase.getFollowers(currentUserId, userId));
    }

    /**
     * 팔로잉 목록 조회
     * GET /users/{userId}/followings
     */
    @GetMapping("/users/{userId}/followings")
    public ResponseEntity<java.util.List<com.simul.user.application.dto.FollowUserResponse>> getFollowings(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(loadFollowUseCase.getFollowings(currentUserId, userId));
    }
}
