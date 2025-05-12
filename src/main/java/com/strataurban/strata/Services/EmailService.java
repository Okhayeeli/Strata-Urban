package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.v2.RequestResetPasswordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

//    public void sendPasswordResetEmail(String toEmail, String token) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(toEmail);
//            message.setSubject("Password Reset Request");
//            message.setText("To reset your password, click the link below:\n\n" +
//                    "http://your-frontend-url/reset-password?token=" + token);
//
//            mailSender.send(message);
//            log.info("Password reset email sent successfully to: {}", toEmail);
//        } catch (MailException e) {
//            log.error("Failed to send password reset email to: {}", toEmail, e);
//            throw new RuntimeException("Failed to send password reset email", e);
//        }
//    }


        public RequestResetPasswordResponse sendPasswordResetEmail(String to, String token) {

            RequestResetPasswordResponse response = new RequestResetPasswordResponse();
            response.setSubject("Password Reset Request");
            response.setSuccess(true);
            response.setMessage("Use the following token to reset your password: This token will expire in 15 minutes.");
            response.setToken(token);
            log.info("Sending email to {}: Subject: {}, Body: {}", to, response.getSubject(), response.getMessage());
            return response;
        }

}