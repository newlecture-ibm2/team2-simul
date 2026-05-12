package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.ReportPersistencePort;
import com.simul.post.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements ReportPersistencePort {

    private final ReportJpaRepository reportJpaRepository;

    @Override
    public Report save(Report report) {
        return reportJpaRepository.save(report);
    }

    @Override
    public boolean existsByPostIdAndReporterId(UUID postId, UUID reporterId) {
        return reportJpaRepository.existsByPostIdAndReporterId(postId, reporterId);
    }

    @Override
    public int countByPostId(UUID postId) {
        return reportJpaRepository.countByPostId(postId);
    }
}
