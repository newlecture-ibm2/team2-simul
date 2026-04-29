package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.PostRepositoryPort;
import com.simul.post.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepositoryPort {

    private final PostJpaRepository postJpaRepository;
    private final PostPersistenceMapper postPersistenceMapper;

    @Override
    public Post save(Post post) {
        PostJpaEntity jpaEntity = postPersistenceMapper.mapToJpaEntity(post);
        PostJpaEntity savedEntity = postJpaRepository.save(jpaEntity);
        return postPersistenceMapper.mapToDomainEntity(savedEntity);
    }

    @Override
    public Optional<Post> findById(UUID postId) {
        return postJpaRepository.findById(postId)
                .map(postPersistenceMapper::mapToDomainEntity);
    }

    @Override
    public void deleteById(UUID postId) {
        postJpaRepository.findById(postId).ifPresent(entity -> {
            entity.softDelete();
            postJpaRepository.save(entity);
        });
    }
}
