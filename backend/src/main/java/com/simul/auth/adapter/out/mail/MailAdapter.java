package com.simul.auth.adapter.out.mail;

import com.simul.auth.application.port.out.MailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
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

    @Override
    public void sendVerificationEmail(String to, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[Simul] 이메일 인증을 완료해주세요");

            String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; padding: 40px; background-color: #f9f9f9; text-align: center;\">" +
                    "<div style=\"max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 40px 40px 30px 40px; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border: 1px solid #eaeaea; border-top: 2px solid #95b358; text-align: left;\">" +
                    "<img src=\"" + frontendUrl + "/logo.png\" alt=\"Simul Logo\" style=\"max-width: 100px; margin-bottom: 30px; display: block;\" />" +
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
}
