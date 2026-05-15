package com.simul.post.application.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CreatePostCommand {
    private UUID userId; // Currently from JWT or mock
    private List<MultipartFile> images;
    private String caption;
    private Boolean isPublic;
    private java.util.Map<Integer, java.util.List<String>> newImageTagsMap;
    private List<String> manualTags;
    private UUID baseImageId; // Optional, if from TryOn
    private UUID itemId; // Optional
}
