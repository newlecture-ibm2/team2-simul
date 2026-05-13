package com.simul.tag.application.service;

import com.simul.tag.application.dto.TagResponse;
import com.simul.tag.application.port.in.SearchTagUseCase;
import com.simul.tag.application.port.out.TagPersistencePort;
import com.simul.tag.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchTagService implements SearchTagUseCase {

    private final TagPersistencePort tagPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> searchTags(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String sanitizedQuery = query.trim().toLowerCase();
        
        List<Tag> tags = tagPersistencePort.findTagsByNamePrefix(sanitizedQuery, 10);
        
        return tags.stream()
                .map(tag -> new TagResponse(
                        tag.getId(),
                        tag.getName(),
                        tag.getCategory(),
                        tag.getUsageCount()
                ))
                .toList();
    }
}
