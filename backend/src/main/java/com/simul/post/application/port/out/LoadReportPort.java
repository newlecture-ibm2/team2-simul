package com.simul.post.application.port.out;

import com.simul.post.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadReportPort {
    Page<Report> loadAllReports(Pageable pageable);
}
