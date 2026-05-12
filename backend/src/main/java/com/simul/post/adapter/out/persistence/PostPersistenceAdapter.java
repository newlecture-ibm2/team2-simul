package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public Optional<Post> findById(UUID postId) {
        return postJpaRepository.findById(postId);
    }

    @Override
    public void deleteById(UUID postId) {
        postJpaRepository.findById(postId).ifPresent(entity -> {
            entity.softDelete();
            postJpaRepository.save(entity);
        });
    }

    @Override
    public Page<Post> findAllPublicPosts(Pageable pageable) {
        return postJpaRepository.findAllByIsPublicTrueAndIsBlindedFalse(pageable);
    }

    @Override
    public Page<Post> findFollowingPosts(List<UUID> userIds, Pageable pageable) {
        if (userIds == null || userIds.isEmpty()) return Page.empty(pageable);
        return postJpaRepository.findAllByUserIdInAndIsPublicTrueAndIsBlindedFalse(userIds, pageable);
    }

    @Override
    public long countByUserId(UUID userId) {
        return postJpaRepository.countByUserId(userId);
    }

    @Override
    public Page<Post> findByUserId(UUID userId, Pageable pageable) {
        return postJpaRepository.findAllByUserId(userId, pageable);
    }

    @Override
    public Page<Post> findPublicPostsByUserId(UUID userId, Pageable pageable) {
        return postJpaRepository.findAllByUserIdAndIsPublicTrue(userId, pageable);
    }
}
