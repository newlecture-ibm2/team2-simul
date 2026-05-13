package com.simul.user.application.port.in;

import java.util.UUID;

public interface SuspendUserUseCase {
    /**
     * 악성 유저 정지
     *
     * @param userId 정지할 유저의 ID
     */
    void suspendUser(UUID userId);
}
