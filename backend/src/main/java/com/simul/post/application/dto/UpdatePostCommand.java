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
    private final java.util.Map<String, java.util.List<String>> existingImageTagsMap;
    private final java.util.Map<Integer, java.util.List<String>> newImageTagsMap;
    private final List<String> manualTags;
    private final List<String> existingImageUrls;
    private final List<org.springframework.web.multipart.MultipartFile> newImages;
    private final List<String> imageOrder;
}
