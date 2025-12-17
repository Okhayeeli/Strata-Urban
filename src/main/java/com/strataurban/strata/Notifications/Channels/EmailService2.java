package com.strataurban.strata.Notifications.Channels;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.strataurban.strata.DTOs.v2.RequestResetPasswordResponse;
import com.strataurban.strata.Entities.Providers.Offer;
import com.strataurban.strata.Services.EmailVerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService2 {

    private final EmailVerificationTokenService emailVerificationTokenService;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${recipient.email}")
    private String recipientEmail;

    @Value("${email.enabled:true}")
    private boolean emailEnabled;

    private Resend getResendClient() {
        return new Resend(resendApiKey);
    }

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

            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>") // Use your verified domain
                    .to(recipientEmail)
                    .subject(subject)
                    .text(body)
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            log.info("Email sent successfully to: {} with subject: {}, ID: {}", to, subject, data.getId());
            return CompletableFuture.completedFuture(true);

        } catch (ResendException e) {
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

            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            log.info("HTML email sent successfully to: {} with subject: {}, ID: {}", to, subject, data.getId());
            return CompletableFuture.completedFuture(true);

        } catch (ResendException e) {
            log.error("Failed to send HTML email to: {} with subject: {}", to, subject, e);
            return CompletableFuture.completedFuture(false);
        }
    }

    // ===== SYNCHRONOUS METHODS =====

    public RequestResetPasswordResponse sendPasswordResetEmail(String toEmail, String token) {
        try {
            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(toEmail)
                    .subject("Password Reset Request")
                    .text("To reset your password, use the token below:\n\n" + token)
                    .build();

            resend.emails().send(params);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (ResendException e) {
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
            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(recipientEmail)
                    .subject("Password Reset Request")
                    .text("To reset your password, use the token below:\n\n" + token)
                    .build();

            resend.emails().send(params);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (ResendException e) {
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
            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(recipientEmail)
                    .subject(subject)
                    .text(body)
                    .build();

            resend.emails().send(params);
            log.info("Simple email sent to: {}", to);
        } catch (ResendException e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(recipientEmail)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(params);
            log.info("HTML email sent to: {}", to);
        } catch (ResendException e) {
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
        try {
            String subject = "Booking Offer Sent by " + providerName;
            String body = "An offer has been sent by " + providerName +
                    "\nBelow are the details of the offer: " + offerDetails.getFormattedPriceWithDiscount() +
                    " for the duration of " + offerDetails.getEstimatedDuration() +
                    " the offer expires on " + offerDetails.getValidUntil();

            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(recipientEmail)
                    .subject(subject)
                    .text(body)
                    .build();

            resend.emails().send(params);
            log.info("Offer email sent successfully to: {}", to);
        } catch (ResendException e) {
            log.error("Failed to send offer email to: {}", to, e);
            throw new RuntimeException("Failed to send offer email", e);
        }
    }

    public void standbyEmail(String toEmail) {
        RequestResetPasswordResponse response = new RequestResetPasswordResponse();
        response.setSubject("Password Reset Request");
        response.setSuccess(true);
        response.setMessage("A token has been sent to your email");
        log.info("Sending email to {}: Subject: {}, Body: {}", toEmail, response.getSubject(), response.getMessage());
    }
}