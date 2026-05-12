package com.simul.post.application.port.in;

import com.simul.post.application.dto.ReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetReportsUseCase {
    Page<ReportResponse> getReports(Pageable pageable);
}
