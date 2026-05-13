package com.simul.user.adapter.in.web;

import com.simul.user.application.dto.UserProfileResponse;
import com.simul.user.application.dto.UserResponse;
import com.simul.user.application.port.in.LoadUserUseCase;
import com.simul.user.application.port.in.UpdateUserUseCase;
import com.simul.user.application.port.in.WithdrawUserUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * 사용자 관리 API 컨트롤러
 *
 * 엔드포인트:
 * - GET /users/me      → 내 프로필 조회 (팔로우 수 포함)
 * - PATCH /users/me    → 프로필 수정
 * - DELETE /users/me   → 회원 탈퇴
 * - GET /users/{id}    → 타인 프로필 조회 (팔로우 수 + 팔로우 여부 포함)
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final LoadUserUseCase loadUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final WithdrawUserUseCase withdrawUserUseCase;
    private final com.simul.post.application.port.in.GetUserPostsUseCase getUserPostsUseCase;

    public UserController(
        LoadUserUseCase loadUserUseCase,
        UpdateUserUseCase updateUserUseCase,
        WithdrawUserUseCase withdrawUserUseCase,
        com.simul.post.application.port.in.GetUserPostsUseCase getUserPostsUseCase
    ) {
        this.loadUserUseCase = loadUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.withdrawUserUseCase = withdrawUserUseCase;
        this.getUserPostsUseCase = getUserPostsUseCase;
    }

    /**
     * 내 프로필 조회 (팔로우 수 포함)
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyInfo(@AuthenticationPrincipal UUID userId) {
        long postCount = getUserPostsUseCase.countUserPosts(userId);
        UserProfileResponse response = loadUserUseCase.loadUserProfile(userId, userId, postCount);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProfile(
        @AuthenticationPrincipal UUID userId,
        @RequestPart(value = "data", required = false) UpdateUserRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
        @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage
    ) {
        String nickname = (request != null) ? request.nickname() : null;
        String name = (request != null) ? request.name() : null;
        com.simul.user.domain.model.Gender gender = (request != null) ? request.gender() : null;
        String bio = (request != null) ? request.bio() : null;
        String profileImageUrl = (request != null) ? request.profileImageUrl() : null;
        String bannerImageUrl = (request != null) ? request.bannerImageUrl() : null;

        updateUserUseCase.updateProfile(
            userId,
            nickname,
            name,
            gender,
            bio,
            profileImageUrl,
            profileImage,
            bannerImageUrl,
            bannerImage
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴 (소프트 딜리트)
     * DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal UUID userId) {
        withdrawUserUseCase.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 타인 프로필 조회 (팔로우 수 + 팔로우 여부 포함)
     * GET /users/{userId}
     * - 비로그인 사용자는 currentUserId가 null로 전달됨
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserInfo(
        @AuthenticationPrincipal UUID currentUserId,
        @PathVariable UUID userId
    ) {
        long postCount = getUserPostsUseCase.countUserPosts(userId);
        UserProfileResponse response = loadUserUseCase.loadUserProfile(userId, currentUserId, postCount);
        return ResponseEntity.ok(response);
    }
}
