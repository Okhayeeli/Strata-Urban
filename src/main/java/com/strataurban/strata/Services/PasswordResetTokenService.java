package com.strataurban.strata.Services;

import com.strataurban.strata.Entities.Generics.PasswordResetToken;
import com.strataurban.strata.Repositories.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetToken createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                userId,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        return passwordResetTokenRepository.save(resetToken);
    }

    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
    }

    public void validateToken(PasswordResetToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Token has expired");
        }
    }
}