package com.simul.tag.adapter.out.persistence;

import com.simul.tag.domain.model.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface PostTagJpaRepository extends JpaRepository<PostTag, UUID> {
    List<PostTag> findByPostId(UUID postId);
    List<PostTag> findByPostIdIn(List<UUID> postIds);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM PostTag pt WHERE pt.postId = :postId")
    void deleteByPostId(@org.springframework.data.repository.query.Param("postId") UUID postId);
}
