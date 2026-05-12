package com.simul.tag.application.service;

import com.simul.tag.application.port.in.AttachTagsToPostUseCase;
import com.simul.tag.application.port.out.TagPersistencePort;
import com.simul.tag.domain.model.PostTag;
import com.simul.tag.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachTagsToPostService implements AttachTagsToPostUseCase {

    private final TagPersistencePort tagPersistencePort;

    @Override
    @Transactional
    public void attachTags(UUID postId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }

        for (String tagName : tagNames) {
            String sanitizedTagName = tagName.trim().toLowerCase();
            if (sanitizedTagName.isEmpty()) continue;

            Tag tag = tagPersistencePort.findByName(sanitizedTagName)
                    .orElseGet(() -> tagPersistencePort.saveTag(
                            Tag.builder()
                                    .name(sanitizedTagName)
                                    .build()
                    ));

            tag.incrementUsageCount();
            tagPersistencePort.saveTag(tag);

            PostTag postTag = PostTag.builder()
                    .postId(postId)
                    .tag(tag)
                    .build();
            tagPersistencePort.savePostTag(postTag);
        }
    }

    @Override
    @Transactional
    public void updateTags(UUID postId, List<String> tagNames) {
        // 기존 태그 조회 및 usageCount 감소
        List<PostTag> existingPostTags = tagPersistencePort.findPostTagsByPostId(postId);
        for (PostTag postTag : existingPostTags) {
            Tag tag = postTag.getTag();
            tag.decrementUsageCount();
            tagPersistencePort.saveTag(tag);
        }

        // 기존 태그 매핑 모두 삭제
        tagPersistencePort.deletePostTagsByPostId(postId);
        // 새 태그 매핑
        attachTags(postId, tagNames);
    }
}
