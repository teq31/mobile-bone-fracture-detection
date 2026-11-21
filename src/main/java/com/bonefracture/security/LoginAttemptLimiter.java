package com.bonefracture.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginAttemptLimiter {

    public static final int MAX_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkNotLocked(String email) {
        if (email == null) {
            return;
        }
        Attempt attempt = attempts.get(key(email));
        if (attempt != null && attempt.isLocked()) {
            throw new TooManyLoginAttemptsException(
                    "too many failed login attempts, try again later");
        }
    }

    public void recordFailure(String email) {
        if (email == null) {
            return;
        }
        attempts.compute(key(email), (k, current) -> {
            Attempt attempt = (current == null) ? new Attempt() : current;
            attempt.registerFailure();
            return attempt;
        });
    }

    public void recordSuccess(String email) {
        if (email == null) {
            return;
        }
        attempts.remove(key(email));
    }

    private String key(String email) {
        return email.trim().toLowerCase();
    }

    private static final class Attempt {
        private int failures;
        private Instant lockedUntil;

        void registerFailure() {
            failures++;
            if (failures >= MAX_ATTEMPTS) {
                lockedUntil = Instant.now().plus(LOCK_DURATION);
            }
        }

        boolean isLocked() {
            if (lockedUntil == null) {
                return false;
            }
            if (Instant.now().isAfter(lockedUntil)) {
                failures = 0;
                lockedUntil = null;
                return false;
            }
            return true;
        }
    }
}
