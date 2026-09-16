package vn.tera.learn.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.tera.learn.exception.AuthFlowException;
import vn.tera.learn.service.VerificationEmailSender;

@Service
public class SmtpVerificationEmailSender implements VerificationEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpVerificationEmailSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean deliveryEnabled;
    private final String fromAddress;

    public SmtpVerificationEmailSender(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.delivery-enabled:false}") boolean deliveryEnabled,
            @Value("${app.mail.from:no-reply@tera.vn}") String fromAddress
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.deliveryEnabled = deliveryEnabled;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationCode(String email, String code, long expiresInMinutes) {
        if (!deliveryEnabled) {
            LOGGER.info("Development email verification code for {} is {}", email, code);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new AuthFlowException(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_NOT_CONFIGURED", "Dịch vụ gửi email chưa được cấu hình");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Mã xác thực tài khoản Tera");
        message.setText("Mã xác thực Tera của bạn là: " + code
                + "\n\nMã có hiệu lực trong " + expiresInMinutes + " phút."
                + "\nNếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new AuthFlowException(HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_DELIVERY_FAILED", "Không thể gửi email xác thực lúc này");
        }
    }
}
