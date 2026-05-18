package com.simul.auth.application.port.in;

public interface FindPasswordUseCase {
    void requestResetCode(String email);
    void verifyResetCode(String email, String code);
    void resetPassword(String email, String code, String newPassword);
}
