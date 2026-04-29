package com.simul.post.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class Post {
    private UUID postId;
    private UUID userId;
    private UUID baseImageId;
    private UUID itemId;
    private String imageUrl;
    private PostStatus status;
    private String caption;
    private Boolean isPublic;
    private Boolean isBlinded;
    private Integer reportCount;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private List<PostImage> images;
}
