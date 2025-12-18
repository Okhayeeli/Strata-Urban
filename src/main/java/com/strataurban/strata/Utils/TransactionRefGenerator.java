package com.strataurban.strata.Utils;

import java.security.SecureRandom;

public final class TransactionRefGenerator {

    private static final String PREFIX = "STRX";
    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TransactionRefGenerator() {
    }

    public static String generate() {
        return String.format(
                "%s-%s-%s-%s",
                PREFIX,
                randomBlock(),
                randomBlock(),
                randomBlock()
        );
    }

    private static String randomBlock() {
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }
}

