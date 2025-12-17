package com.strataurban.strata.Services;

import com.strataurban.strata.DTOs.v2.RequestResetPasswordResponse;
import com.strataurban.strata.Entities.Generics.PasswordResetToken;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.PasswordResetTokenRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
import com.strataurban.strata.Services.v2.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserServiceImpl userServiceImpl;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    @Value("${environment.awareness}")
    private String environmentAwareness;

    @Value("${recipient.email}")
    private String recipientEmail;

//    public PasswordResetToken createToken(Long userId) {
//        String token = UUID.randomUUID().toString();
//        PasswordResetToken resetToken = new PasswordResetToken(
//                token,
//                userId,
//                Instant.now().plus(24, ChronoUnit.HOURS)
//        );
//        return passwordResetTokenRepository.save(resetToken);
//    }
//
//    public PasswordResetToken findByToken(String token) {
//        return passwordResetTokenRepository.findByToken(token)
//                .orElseThrow(() -> new RuntimeException("Invalid token"));
//    }
//
//    public void validateToken(PasswordResetToken token) {
//        if (token.getExpiryDate().isBefore(Instant.now())) {
//            throw new RuntimeException("Token has expired");
//        }
//    }



    public RequestResetPasswordResponse requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate a random token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);

        // Save the token
        passwordResetTokenRepository.save(resetToken);

        if(!environmentAwareness.equalsIgnoreCase("prod")){
           return emailService.testSendPasswordResetEmail(recipientEmail, token);
        }
        // Send email with the token
        return emailService.sendPasswordResetEmail(user.getEmail(), token);
    }



    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        userServiceImpl.validatePassword(newPassword);
//        userService.checkPasswordHistory(user, newPassword);
        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPassword(newPasswordHash);
        user.setLastPasswordChange(LocalDateTime.now());
//        userService.savePasswordHistory(user, newPasswordHash);
        userService.resetFailedLoginAttempts(user);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

}