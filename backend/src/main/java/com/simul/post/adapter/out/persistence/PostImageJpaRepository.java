package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PostImageJpaRepository extends JpaRepository<PostImage, UUID> {
}
