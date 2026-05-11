package com.simul.post.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UpdatePostCommand {
    private final UUID postId;
    private final UUID userId;
    private final String caption;
    private final Boolean isPublic;
    private final List<String> tags;
    private final List<String> existingImageUrls;
    private final List<org.springframework.web.multipart.MultipartFile> newImages;
}
