package com.simul.closet.application.dto;

import com.simul.closet.domain.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosetItemResponse {
    private UUID itemId;
    private UUID imageId;
    private String imageUrl;
    private Category category;
    private String memo;
    private Integer tryCount;
    private List<UUID> collectionIds;
    private LocalDateTime createdAt;
}
