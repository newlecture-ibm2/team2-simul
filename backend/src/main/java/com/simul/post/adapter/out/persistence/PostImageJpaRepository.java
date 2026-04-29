package com.simul.post.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PostImageJpaRepository extends JpaRepository<PostImageJpaEntity, UUID> {
}
