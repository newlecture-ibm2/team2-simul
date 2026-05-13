package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.PostReportPersistencePort;
import com.simul.post.domain.model.PostReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostReportPersistenceAdapter implements PostReportPersistencePort {

    private final PostReportJpaRepository postReportJpaRepository;

    @Override
    public PostReport save(PostReport report) {
        return postReportJpaRepository.save(report);
    }

    @Override
    public boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId) {
        return postReportJpaRepository.existsByPostIdAndReporterId(postId, reporterId);
    }

    @Override
    public int countByPostId(UUID postId) {
        return postReportJpaRepository.countByPostId(postId);
    }

    @Override
    public Page<PostReport> loadAllReports(Pageable pageable) {
        return postReportJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
