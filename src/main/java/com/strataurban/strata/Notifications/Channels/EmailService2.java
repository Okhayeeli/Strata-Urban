package com.strataurban.strata.Notifications.Channels;

import com.strataurban.strata.DTOs.v2.RequestResetPasswordResponse;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Services.EmailVerificationTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService2 {


    private final JavaMailSender mailSender;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${recipient.email}")
    private String recipientEmail;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send simple email asynchronously (for notification system)
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendEmailAsync(String to, String subject, String body) {
        try {
            if (!emailEnabled) {
                log.warn("Email is disabled. Would have sent to {}: {}", to, subject);
                return CompletableFuture.completedFuture(false);
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {} with subject: {}", to, subject);
            return CompletableFuture.completedFuture(true);

        } catch (MailException e) {
            log.error("Failed to send email to: {} with subject: {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send HTML email asynchronously (for notification system)
     */
    @Async("notificationExecutor")
    public CompletableFuture<Boolean> sendHtmlEmailAsync(String to, String subject, String htmlBody) {
        try {
            if (!emailEnabled) {
                log.warn("Email is disabled. Would have sent HTML to {}: {}", to, subject);
                return CompletableFuture.completedFuture(false);
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {} with subject: {}", to, subject);
            return CompletableFuture.completedFuture(true);

        } catch (MessagingException | MailException e) {
            log.error("Failed to send HTML email to: {} with subject: {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===== SYNCHRONOUS METHODS (Existing functionality) =====

    public RequestResetPasswordResponse sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, use the token below:\n\n" + token);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }

        RequestResetPasswordResponse response = new RequestResetPasswordResponse();
        response.setSubject("Password Reset Request");
        response.setSuccess(true);
        response.setMessage("A token has been sent to your email");
        log.info("Sending email to {}: Subject: {}, Body: {}", toEmail, response.getSubject(), response.getMessage());
        return response;
    }

    public RequestResetPasswordResponse testSendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, use the token below:\n\n" + token);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }

        RequestResetPasswordResponse response = new RequestResetPasswordResponse();
        response.setSubject("Password Reset Request");
        response.setSuccess(true);
        response.setMessage("A token has been sent to your email");
        response.setTestToken(token);
        log.info("Sending email to {}: Subject: {}, Body: {}", toEmail, response.getSubject(), response.getMessage());
        return response;
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            log.info("HTML email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    public void sendVerificationEmail(String to, Long userId) {
        String token = emailVerificationTokenService.generateToken(userId);
        String subject = "Verify Your Email";
        String verificationUrl = "http://your-frontend-url/verify-email?token=" + token;

        String htmlBody = "<h2>Verify Your Email</h2>" +
                "<p>Click the link below to verify your email:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Now</a>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    public String testSendVerificationEmail(String to, Long userId) {
        String token = emailVerificationTokenService.generateToken(userId);
        String subject = "Verify Your Email";
        String verificationUrl = "http://your-frontend-url/verify-email?token=" + token;

        String htmlBody = "<h2>Verify Your Email</h2>" +
                "<p>Click the link below to verify your email:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Now</a>";

        sendHtmlEmail(recipientEmail, subject, htmlBody);
        return token;
    }

    public void sendSubmissionEmail(String to, String submissionDetails) {
        String subject = "Submission Received";
        String htmlBody = "<h3>Your submission has been received!</h3>" +
                "<p>Details: " + submissionDetails + "</p>";
        sendHtmlEmail(recipientEmail, subject, htmlBody);
    }

    public void sendApprovalEmail(String to, String approvalDetails) {
        String subject = "Your Request Has Been Approved!";
        String htmlBody = "<h3>Congratulations!</h3>" +
                "<p>Your request has been approved:</p>" +
                "<p>" + approvalDetails + "</p>";
        sendHtmlEmail(recipientEmail, subject, htmlBody);
    }

    public void sendOfferEmail(String to, Offer offerDetails, String providerName) {
        String subject = "Booking Offer Sent by " + providerName;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText("An offer has been sent by " + providerName +
                "\nBelow are the details of the offer: " + offerDetails.getPrice() +
                " for the duration of " + offerDetails.getEstimatedDuration() +
                " the offer expires on " + offerDetails.getValidUntil());

        mailSender.send(message);
        log.info("Offer email sent successfully to: {}", to);
    }

    public void standbyEmail(String toEmail) {
        RequestResetPasswordResponse response = new RequestResetPasswordResponse();
        response.setSubject("Password Reset Request");
        response.setSuccess(true);
        response.setMessage("A token has been sent to your email");
        log.info("Sending email to {}: Subject: {}, Body: {}", toEmail, response.getSubject(), response.getMessage());
    }
}