package com.simul.post.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PostImage {
    private UUID postImageId;
    private UUID postId;
    private String imageUrl;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
