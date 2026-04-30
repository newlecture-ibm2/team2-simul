package com.simul.tag.adapter.out.persistence;

import com.simul.tag.domain.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PostTagJpaRepository extends JpaRepository<PostTag, UUID> {
    List<PostTag> findByPostId(UUID postId);
    List<PostTag> findByPostIdIn(List<UUID> postIds);
    void deleteByPostId(UUID postId);
}
