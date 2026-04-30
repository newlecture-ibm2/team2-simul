package com.simul.tag.application.service;

import com.simul.tag.application.port.in.LoadTagsUseCase;
import com.simul.tag.application.port.out.TagPersistencePort;
import com.simul.tag.domain.model.PostTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
}
