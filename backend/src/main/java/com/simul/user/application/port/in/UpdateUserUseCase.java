package com.simul.user.application.port.in;

import com.simul.user.domain.model.Gender;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

/**
 * 사용자 프로필 수정 유즈케이스 (Input Port)
 */
public interface UpdateUserUseCase {
    void updateProfile(UUID userId, String nickname, String name, Gender gender, String bio, String profileImageUrl, MultipartFile profileImage, String bannerImageUrl, MultipartFile bannerImage);
}
