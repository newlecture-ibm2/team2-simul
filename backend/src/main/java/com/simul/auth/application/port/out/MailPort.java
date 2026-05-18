package com.simul.auth.application.port.out;

/**
 * 외부 메일 발송 시스템을 위한 Output Port
 */
public interface MailPort {
    /**
     * 회원가입 인증 메일을 발송합니다.
     * @param to 수신자 이메일
     * @param verificationLink 인증 링크
     */
    void sendVerificationEmail(String to, String verificationLink);

    /**
     * 비밀번호 재설정 인증코드 메일을 발송합니다.
     * @param to 수신자 이메일
     * @param code 6자리 인증코드
     */
    void sendPasswordResetCode(String to, String code);
}
