package com.bonefracture.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptLimiterTest {

    @Test
    void allowsLoginBelowThreshold() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int i = 0; i < LoginAttemptLimiter.MAX_ATTEMPTS - 1; i++) {
            limiter.recordFailure("user@example.com");
        }
        assertDoesNotThrow(() -> limiter.checkNotLocked("user@example.com"));
    }

    @Test
    void locksAfterTooManyFailures() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int i = 0; i < LoginAttemptLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordFailure("user@example.com");
        }
        assertThrows(TooManyLoginAttemptsException.class,
                () -> limiter.checkNotLocked("user@example.com"));
    }

    @Test
    void successResetsTheCounter() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int i = 0; i < LoginAttemptLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordFailure("user@example.com");
        }
        limiter.recordSuccess("user@example.com");
        assertDoesNotThrow(() -> limiter.checkNotLocked("user@example.com"));
    }

    @Test
    void differentEmailsAreIndependent() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int i = 0; i < LoginAttemptLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordFailure("locked@example.com");
        }
        assertDoesNotThrow(() -> limiter.checkNotLocked("other@example.com"));
    }

    @Test
    void emailMatchingIsCaseInsensitive() {
        LoginAttemptLimiter limiter = new LoginAttemptLimiter();
        for (int i = 0; i < LoginAttemptLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordFailure("User@Example.com");
        }
        assertThrows(TooManyLoginAttemptsException.class,
                () -> limiter.checkNotLocked("user@example.com"));
    }
}
