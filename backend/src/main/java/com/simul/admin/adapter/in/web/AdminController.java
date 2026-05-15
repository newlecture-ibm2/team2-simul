package com.simul.admin.adapter.in.web;

import com.simul.post.application.dto.ReportResponse;
import com.simul.post.application.port.in.GetReportsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import com.simul.post.application.port.in.BlindPostUseCase;
import com.simul.post.application.port.in.UnblindPostUseCase;
import com.simul.user.application.port.in.SuspendUserUseCase;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.UUID;
import com.simul.tryon.application.port.in.ProvideTryonCreditsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GetReportsUseCase getReportsUseCase;
    private final BlindPostUseCase blindPostUseCase;
    private final UnblindPostUseCase unblindPostUseCase;
    private final SuspendUserUseCase suspendUserUseCase;
    private final ProvideTryonCreditsUseCase provideTryonCreditsUseCase;
    private final com.simul.user.application.port.in.LoadAllUsersUseCase loadAllUsersUseCase;

    @GetMapping("/users")
    public ResponseEntity<Page<com.simul.admin.application.dto.AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<com.simul.admin.application.dto.AdminUserResponse> users = loadAllUsersUseCase.loadAllUsers(pageRequest);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<ReportResponse>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ReportResponse> reports = getReportsUseCase.getReports(pageRequest);
        
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/posts/{postId}/blind")
    public ResponseEntity<Void> blindPost(@PathVariable UUID postId) {
        blindPostUseCase.blindPost(postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/posts/{postId}/unblind")
    public ResponseEntity<Void> unblindPost(@PathVariable UUID postId) {
        unblindPostUseCase.unblindPost(postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(@PathVariable UUID userId) {
        suspendUserUseCase.suspendUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/credits")
    public ResponseEntity<Void> provideCredits(@PathVariable UUID userId) {
        provideTryonCreditsUseCase.provideCredits(userId);
        return ResponseEntity.ok().build();
    }
}
