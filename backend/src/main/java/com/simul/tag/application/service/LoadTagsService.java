package com.simul.tag.application.service;

import com.simul.tag.application.port.in.LoadTagsUseCase;
import com.simul.tag.application.port.out.TagPersistencePort;
import com.simul.tag.domain.model.PostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.simul.tag.application.dto.PostTagsResponse;

@Service
@RequiredArgsConstructor
public class LoadTagsService implements LoadTagsUseCase {

    private final TagPersistencePort tagPersistencePort;

    @Override
    public Map<UUID, List<String>> loadTagsByPostIds(List<UUID> postIds) {
        List<PostTag> postTags = tagPersistencePort.findPostTagsByPostIds(postIds);
        
        return postTags.stream()
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
                        Collectors.mapping(pt -> pt.getTag().getName(), Collectors.toList())
                ));
    }

    @Override
    public PostTagsResponse loadDetailedTagsByPostId(UUID postId) {
        List<PostTag> postTags = tagPersistencePort.findPostTagsByPostId(postId);
        
        List<String> tags = new ArrayList<>();
        Map<String, List<String>> imageTagsMap = new HashMap<>();
        List<String> manualTags = new ArrayList<>();

        for (PostTag pt : postTags) {
            String tagName = pt.getTag().getName();
            tags.add(tagName);
            
            if (pt.getSourceImageUrl() != null) {
                imageTagsMap.computeIfAbsent(pt.getSourceImageUrl(), k -> new ArrayList<>()).add(tagName);
            } else {
                manualTags.add(tagName);
            }
        }
        
        return new PostTagsResponse(tags, imageTagsMap, manualTags);
    }
}
