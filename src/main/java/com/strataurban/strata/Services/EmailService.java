package com.strataurban.strata.Services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.strataurban.strata.DTOs.v2.RequestResetPasswordResponse;
import com.strataurban.strata.Entities.Providers.Offer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailVerificationTokenService emailVerificationTokenService;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${recipient.email}")
    private String recipientEmail;

    private Resend getResendClient() {
        return new Resend(resendApiKey);
    }

    public RequestResetPasswordResponse sendPasswordResetEmail(String toEmail, String token) {
        try {
            Resend resend = getResendClient();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Strata Urban <onboarding@resend.dev>")
                    .to(toEmail)
                    .subject("Password Reset Request")
                    .text("To reset your password, use the token below:\n\n" + token)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Password reset email sent successfully to: {}, ID: {}", toEmail, response.getId());
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

            CreateEmailResponse emailResponse = resend.emails().send(params);
            log.info("Password reset email sent successfully to: {}, ID: {}", toEmail, emailResponse.getId());
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

    /**
     * Send an HTML email (for verification, approval, etc.)
     */
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

    /**
     * Use cases below (examples you can call in your services)
     */

    // Email Verification
    public void sendVerificationEmail(String to, Long userId) {
        String token = emailVerificationTokenService.generateToken(userId);
        String subject = "Verify Your Email";
        String verificationUrl = "http://your-frontend-url/verify-email?token=" + token;

        String htmlBody = "<h2>Verify Your Email</h2>" +
                "<p>Click the link below to verify your email:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Now</a>";

        sendHtmlEmail(to, subject, htmlBody);
    }

    // Email Verification (Test)
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

    // Submission Notification
    public void sendSubmissionEmail(String to, String submissionDetails) {
        String subject = "Submission Received";
        String htmlBody = "<h3>Your submission has been received!</h3>" +
                "<p>Details: " + submissionDetails + "</p>";
        sendHtmlEmail(recipientEmail, subject, htmlBody);
    }

    // Approval Notification
    public void sendApprovalEmail(String to, String approvalDetails) {
        String subject = "Your Request Has Been Approved!";
        String htmlBody = "<h3>Congratulations!</h3>" +
                "<p>Your request has been approved:</p>" +
                "<p>" + approvalDetails + "</p>";
        sendHtmlEmail(recipientEmail, subject, htmlBody);
    }

    public void sendOfferEmail(String to, Offer offerDetails, String ProviderName) {
        try {
            String subject = "Booking Offer Sent by " + ProviderName;
            String body = "An offer has been sent by " + ProviderName +
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