package com.simul.admin.adapter.in.web;

import com.simul.post.application.dto.ReportResponse;
import com.simul.post.application.port.in.GetReportsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminController {

    private final GetReportsUseCase getReportsUseCase;

    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ReportResponse> reports = getReportsUseCase.getReports(pageRequest);
        
        return ResponseEntity.ok(reports);
    }
}
