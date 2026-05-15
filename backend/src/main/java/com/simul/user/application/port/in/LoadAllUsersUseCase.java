package com.simul.user.application.port.in;

import com.simul.admin.application.dto.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadAllUsersUseCase {
    Page<AdminUserResponse> loadAllUsers(Pageable pageable);
}
