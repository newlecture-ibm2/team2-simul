package com.simul.post.adapter.out.persistence;

import com.simul.post.application.port.out.LoadReportPort;
import com.simul.post.domain.model.Report;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements LoadReportPort {

    private final ReportJpaRepository reportJpaRepository;

    @Override
    public Page<Report> loadAllReports(Pageable pageable) {
        return reportJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
