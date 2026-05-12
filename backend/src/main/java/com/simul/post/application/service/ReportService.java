package com.simul.post.application.service;

import com.simul.post.application.dto.ReportResponse;
import com.simul.post.application.port.in.GetReportsUseCase;
import com.simul.post.application.port.out.LoadReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService implements GetReportsUseCase {

    private final LoadReportPort loadReportPort;

    @Override
    public Page<ReportResponse> getReports(Pageable pageable) {
        return loadReportPort.loadAllReports(pageable)
                .map(ReportResponse::from);
    }
}
