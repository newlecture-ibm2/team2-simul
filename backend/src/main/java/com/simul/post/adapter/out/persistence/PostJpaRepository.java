package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PostJpaRepository extends JpaRepository<Post, UUID> {
}
