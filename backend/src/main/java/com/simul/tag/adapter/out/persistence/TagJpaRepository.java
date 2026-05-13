package com.simul.tag.adapter.out.persistence;

import com.simul.tag.domain.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagJpaRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByName(String name);
    
    List<Tag> findByNameStartingWithIgnoreCaseOrderByUsageCountDesc(String prefix, org.springframework.data.domain.Pageable pageable);
}
