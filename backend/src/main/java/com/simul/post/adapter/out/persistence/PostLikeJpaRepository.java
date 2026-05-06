package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, UUID> {

    Optional<PostLike> findByPostIdAndUserId(UUID postId, UUID userId);

    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.userId = :userId AND pl.postId IN :postIds")
    List<UUID> findPostIdsByUserIdAndPostIdIn(@Param("userId") UUID userId, @Param("postIds") List<UUID> postIds);
}
