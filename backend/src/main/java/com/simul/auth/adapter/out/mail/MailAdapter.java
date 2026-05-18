package com.simul.auth.adapter.out.mail;

import com.simul.auth.application.port.out.MailPort;
import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * JavaMailSender를 사용하여 실제 메일을 발송하는 Persistence Adapter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailAdapter implements MailPort {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Simul] 이메일 인증을 완료해주세요");

            String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; padding: 40px; background-color: #f9f9f9; text-align: center;\">" +
                    "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 40px 40px 30px 40px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border: 1px solid #eaeaea; border-top: 2px solid #95b358; text-align: left;\">" +
                    "<div style=\"font-size: 28px; font-weight: 900; color: #95b358; font-family: 'Apple SD Gothic Neo', sans-serif; letter-spacing: -1px; margin-bottom: 25px;\">SIMUL</div>" +
                    "<h2 style=\"color: #333333; margin-bottom: 20px; margin-top: 0; font-size: 24px;\">환영합니다!</h2>" +
                    "<p style=\"color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 35px;\">Simul 가입을 위한 이메일 인증을 진행합니다.<br/>아래 버튼을 눌러 인증을 완료해 주세요.</p>" +
                    "<div style=\"text-align: center;\">" +
                    "<a href=\"" + verificationLink + "\" style=\"display: inline-block; padding: 14px 30px; font-size: 16px; font-weight: bold; color: #ffffff; background-color: #95b358; text-decoration: none; border-radius: 99px;\">이메일 인증하기</a>" +
                    "</div>" +
                    "<hr style=\"border: none; border-top: 1px solid #eeeeee; margin: 35px 0 20px 0;\" />" +
                    "<p style=\"font-size: 13px; color: #aaaaaa; margin: 0;\">본인이 가입을 요청하지 않으셨다면 이 메일을 무시해 주세요.</p>" +
                    "</div>" +
                    "</div>";

            helper.setText(htmlContent, true); // true = HTML 적용

            mailSender.send(message);
            log.info("인증 메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("인증 메일 발송 실패: {}", to, e);
            // 메일 발송 실패가 회원가입 전체의 롤백으로 이어지지 않게 하려면 예외를 던지지 않거나,
            // 별도의 비동기 처리를 고려할 수 있습니다.
        }
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Simul] 비밀번호 재설정 인증번호");

            String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; padding: 40px; background-color: #f9f9f9; text-align: center;\">" +
                    "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 40px 40px 30px 40px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border: 1px solid #eaeaea; border-top: 2px solid #95b358; text-align: left;\">" +
                    "<div style=\"font-size: 28px; font-weight: 900; color: #95b358; font-family: 'Apple SD Gothic Neo', sans-serif; letter-spacing: -1px; margin-bottom: 25px;\">SIMUL</div>" +
                    "<h2 style=\"color: #333333; margin-bottom: 20px; margin-top: 0; font-size: 24px;\">비밀번호 찾기 인증번호</h2>" +
                    "<p style=\"color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 25px;\">안녕하세요.<br/>비밀번호 재설정을 위한 인증번호 6자리를 안내해 드립니다.<br/>아래 인증번호를 웹 화면에 입력하여 인증을 진행해 주세요.</p>" +
                    "<div style=\"text-align: center; margin: 30px 0; background-color: #f5f6f2; padding: 20px; border-radius: 8px; border: 1px dashed #95b358;\">" +
                    "<span style=\"font-size: 32px; font-weight: bold; color: #4a6724; letter-spacing: 8px;\">" + code + "</span>" +
                    "</div>" +
                    "<p style=\"font-size: 14px; color: #888888; margin-bottom: 30px;\">본 인증번호는 15분 동안만 유효합니다.</p>" +
                    "<hr style=\"border: none; border-top: 1px solid #eeeeee; margin: 25px 0 20px 0;\" />" +
                    "<p style=\"font-size: 13px; color: #aaaaaa; margin: 0;\">본인이 비밀번호 재설정을 요청하지 않으셨다면 이 메일을 무시해 주세요.</p>" +
                    "</div>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("비밀번호 재설정 인증번호 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("비밀번호 재설정 인증번호 발송 실패: {}", to, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "인증 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
