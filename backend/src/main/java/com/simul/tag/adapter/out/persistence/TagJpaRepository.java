package com.simul.tag.adapter.out.persistence;

import com.simul.tag.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TagJpaRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
}
