package com.strataurban.strata.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class EmailVerificationTokenService {
//    private final EmailVerificationTokenRepository tokenRepository;
//
//    @Value("${app.auth.token.email-verification.expiration}")
//    private long expiration;
//
//    public String generateToken(User user) {
//        String token = UUID.randomUUID().toString();
//        EmailVerificationToken verificationToken = new EmailVerificationToken();
//        verificationToken.setToken(token);
//        verificationToken.setUser(user);
//        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
//
//        tokenRepository.save(verificationToken);
//        return token;
//    }
//
//    public User validateToken(String token) {
//        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
//                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
//
//        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//            tokenRepository.delete(verificationToken);
//            throw new InvalidTokenException("Verification token has expired");
//        }
//
//        User user = verificationToken.getUser();
//        user.setEmailVerified(true);
//        tokenRepository.delete(verificationToken);
//        return user;
//    }
//
//    @Scheduled(cron = "0 0 */1 * * *") // Run every hour
//    public void cleanExpiredTokens() {
//        log.info("Cleaning expired email verification tokens");
//        tokenRepository.deleteAllExpiredSince(LocalDateTime.now());
//    }
}