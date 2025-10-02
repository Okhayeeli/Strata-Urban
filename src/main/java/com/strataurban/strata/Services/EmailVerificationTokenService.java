package com.strataurban.strata.Services;

import com.strataurban.strata.Entities.Generics.EmailVerificationToken;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Exceptions.InvalidTokenException;
import com.strataurban.strata.Repositories.v2.EmailVerificationTokenRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Services.v2.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class EmailVerificationTokenService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

//    @Value("${app.auth.token.email-verification.expiration}")
//    private long expiration;

    public EmailVerificationTokenService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public String generateToken(Long userId) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUserId(userId);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);
        return token;
    }

    public void validateToken(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Verification token has expired");
        }


        User user = userRepository.findById(verificationToken.getUserId()).orElseThrow(() -> new InvalidTokenException("Invalid user"));
        user.setEmailVerified(true);
        tokenRepository.delete(verificationToken);
    }

    @Scheduled(cron = "0 0 */1 * * *") // Run every hour
    public void cleanExpiredTokens() {
        log.info("Cleaning expired email verification tokens");
        tokenRepository.deleteAllExpiredSince(LocalDateTime.now());
    }
}