package com.simul.tag.adapter.out.persistence;

import com.simul.tag.application.port.out.TagPersistencePort;
import com.simul.tag.domain.model.PostTag;
import com.simul.tag.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TagPersistenceAdapter implements TagPersistencePort {

    private final TagJpaRepository tagJpaRepository;
    private final PostTagJpaRepository postTagJpaRepository;

    @Override
    public Optional<Tag> findByName(String name) {
        return tagJpaRepository.findByName(name);
    }

    @Override
    public Tag saveTag(Tag tag) {
        return tagJpaRepository.save(tag);
    }

    @Override
    public PostTag savePostTag(PostTag postTag) {
        return postTagJpaRepository.save(postTag);
    }

    @Override
    public List<PostTag> findPostTagsByPostId(UUID postId) {
        return postTagJpaRepository.findByPostId(postId);
    }

    @Override
    public void deletePostTagsByPostId(UUID postId) {
        postTagJpaRepository.deleteByPostId(postId);
    }

    @Override
    public List<PostTag> findPostTagsByPostIds(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return List.of();
        return postTagJpaRepository.findByPostIdIn(postIds);
    }

    @Override
    public List<Tag> findTagsByNamePrefix(String prefix, int limit) {
        return tagJpaRepository.findByNameStartingWithIgnoreCaseOrderByUsageCountDesc(
                prefix, 
                org.springframework.data.domain.PageRequest.of(0, limit)
        );
    }
}
