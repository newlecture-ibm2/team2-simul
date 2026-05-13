package com.simul.tag.application.port.out;

import com.simul.tag.domain.model.PostTag;
import com.simul.tag.domain.model.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagPersistencePort {
    Optional<Tag> findByName(String name);
    Tag saveTag(Tag tag);
    PostTag savePostTag(PostTag postTag);
    List<PostTag> findPostTagsByPostId(UUID postId);
    List<PostTag> findPostTagsByPostIds(List<UUID> postIds);
    void deletePostTagsByPostId(UUID postId);
    List<Tag> findTagsByNamePrefix(String prefix, int limit);
}
