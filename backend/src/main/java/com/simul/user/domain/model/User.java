package com.simul.user.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사용자 도메인 모델
 * - 순수 Java 객체 (JPA 종속성 제거)
 */
@Getter
@Builder
public class User {

    private UUID userId;
    private String provider;
    private String providerId;
    private String name;
    private String nickname;
    private String password;
    
    @Builder.Default
    private Gender gender = Gender.UNKNOWN;
    
    private String bio;
    private String profileImageUrl;
    private String bannerImageUrl;
    
    @Builder.Default
    private boolean isPublic = true;
    
    @Builder.Default
    private Role role = Role.USER;
    
    @Builder.Default
    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    /**
     * 프로필 수정 (null이 아닌 값만 업데이트)
     */
    public void updateProfile(String nickname, String name, Gender gender, String bio, String profileImageUrl, String bannerImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (name != null) this.name = name;
        if (gender != null) this.gender = gender;
        if (bio != null) this.bio = bio;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        if (bannerImageUrl != null) this.bannerImageUrl = bannerImageUrl;
    }

    /**
     * 회원 탈퇴 (소프트 딜리트 플래그)
     * 실제 deletedAt 처리는 Persistence Adapter에서 수행
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 비밀번호 변경
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 관리자에 의한 악성 유저 정지
     */
    public void suspend() {
        this.isActive = false;
    }
}
