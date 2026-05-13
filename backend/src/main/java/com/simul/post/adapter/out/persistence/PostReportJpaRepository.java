package com.simul.post.adapter.out.persistence;

import com.simul.post.domain.model.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostReportJpaRepository extends JpaRepository<PostReport, UUID> {
    boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId);
    int countByPostId(UUID postId);
    Page<PostReport> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
